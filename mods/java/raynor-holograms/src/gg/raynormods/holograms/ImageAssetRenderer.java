package gg.raynormods.holograms;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ImageAssetRenderer {
    private static final int MAX_DOWNLOAD_BYTES = 10 * 1024 * 1024;
    private static final int MAX_FRAMES = 48;
    private static final int MAX_TEXTURE_SIDE = 1024;
    private static final int MAX_ANIMATED_SIDE = 256;
    private static final long MAX_DECODE_PIXELS = 16_777_216L;
    private static final int MAX_CACHE_ENTRIES = 32;
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(4))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
    private static final Map<String, DecodedImage> CACHE = new LinkedHashMap<>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, DecodedImage> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

    private ImageAssetRenderer() {}

    static RenderedAnimation create(String source, float size) {
        String normalized = source == null ? "" : source.trim();
        if (normalized.isEmpty()) return RenderedAnimation.failure("La fuente de imagen no puede estar vacia.");
        if (!Float.isFinite(size) || size < 0.1f || size > 32.0f) return RenderedAnimation.failure("El tamano debe estar entre 0.1 y 32 bloques.");

        if (normalized.toLowerCase(Locale.ROOT).startsWith("builtin:")) {
            String key = normalized.substring("builtin:".length()).trim();
            ImageCatalog.Entry entry = ImageCatalog.find(key);
            if (entry == null) return RenderedAnimation.failure("Imagen incluida desconocida: " + key);
            ModelAsset asset = ModelAsset.getAssetMap().getAsset(entry.modelAssetId());
            if (asset == null) return RenderedAnimation.failure("No se encontro el modelo incluido: " + entry.modelAssetId());
            return RenderedAnimation.success(List.of(Model.createStaticScaledModel(asset, size)), new int[]{1000}, "builtin:" + key);
        }

        try {
            DecodedImage decoded = decoded(normalized);
            List<Model> models = new ArrayList<>();
            for (BufferedImage frame : decoded.frames()) {
                Model model = PixelArtModelFactory.create(frame, size);
                if (model == null) return RenderedAnimation.failure("No se pudo registrar el modelo de imagen.");
                models.add(model);
            }
            return RenderedAnimation.success(List.copyOf(models), decoded.delaysMs(), decoded.signature());
        } catch (ImageLoadException exception) {
            return RenderedAnimation.failure(exception.getMessage());
        } catch (RuntimeException exception) {
            Throwable cause = exception.getCause();
            return RenderedAnimation.failure(cause == null ? "No se pudo cargar la imagen." : cause.getMessage());
        }
    }

    static String validateSource(String source) {
        RenderedAnimation animation = create(source, 1.0f);
        return animation.success() ? null : animation.error();
    }

    static DecodedImage decoded(String source) {
        String normalized = source == null ? "" : source.trim();
        if (normalized.isEmpty()) throw new ImageLoadException("La fuente de imagen no puede estar vacia.");
        if (normalized.toLowerCase(Locale.ROOT).startsWith("builtin:")) {
            throw new ImageLoadException("Los recursos incluidos ya usan un modelo nativo.");
        }
        try {
            String key = cacheKey(normalized);
            synchronized (CACHE) {
                DecodedImage cached = CACHE.get(key);
                if (cached != null) return cached;
            }
            DecodedImage result = decode(load(normalized));
            synchronized (CACHE) { CACHE.put(key, result); }
            return result;
        } catch (ImageLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ImageLoadException(exception.getMessage(), exception);
        }
    }

    static void clearCache() {
        synchronized (CACHE) { CACHE.clear(); }
    }

    private static String cacheKey(String source) throws Exception {
        String lower = source.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) return source;
        String localName = lower.startsWith("local:") ? source.substring("local:".length()) : source;
        Path root = HologramRuntime.imageDirectory();
        Path file = root.resolve(localName.trim()).normalize();
        if (!file.startsWith(root)) throw new ImageLoadException("La ruta local sale del directorio images.");
        if (!Files.isRegularFile(file)) throw new ImageLoadException("No existe la imagen local: " + file.getFileName());
        return file.toString() + ':' + Files.size(file) + ':' + Files.getLastModifiedTime(file).toMillis();
    }

    private static byte[] load(String source) throws Exception {
        String lower = source.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) return download(URI.create(source));
        String localName = lower.startsWith("local:") ? source.substring("local:".length()) : source;
        Path root = HologramRuntime.imageDirectory();
        Path file = root.resolve(localName.trim()).normalize();
        if (!file.startsWith(root)) throw new ImageLoadException("La ruta local sale del directorio images.");
        if (!Files.isRegularFile(file)) throw new ImageLoadException("No existe la imagen local: " + file.getFileName());
        long size = Files.size(file);
        if (size > MAX_DOWNLOAD_BYTES) throw new ImageLoadException("La imagen supera el limite de 10 MB.");
        return Files.readAllBytes(file);
    }

    private static byte[] download(URI initial) throws Exception {
        URI current = initial;
        for (int redirects = 0; redirects <= 3; redirects++) {
            validateRemoteUri(current);
            HttpRequest request = HttpRequest.newBuilder(current)
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "RaynorHolograms/1.0")
                .GET().build();
            HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String location = response.headers().firstValue("location").orElseThrow(() -> new ImageLoadException("Redireccion de imagen sin destino."));
                current = current.resolve(location);
                continue;
            }
            if (status < 200 || status >= 300) throw new ImageLoadException("El servidor de imagen respondio HTTP " + status + ".");
            try (InputStream input = response.body()) {
                byte[] bytes = input.readNBytes(MAX_DOWNLOAD_BYTES + 1);
                if (bytes.length > MAX_DOWNLOAD_BYTES) throw new ImageLoadException("La imagen supera el limite de 10 MB.");
                return bytes;
            }
        }
        throw new ImageLoadException("Demasiadas redirecciones al descargar la imagen.");
    }

    private static void validateRemoteUri(URI uri) throws Exception {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) throw new ImageLoadException("Solo se permiten URLs http o https.");
        if (uri.getHost() == null || uri.getUserInfo() != null) throw new ImageLoadException("URL de imagen no valida.");
        for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
                throw new ImageLoadException("La URL apunta a una red privada o local.");
            }
        }
    }

    private static DecodedImage decode(byte[] bytes) throws Exception {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (stream == null) throw new ImageLoadException("Formato de imagen no reconocido.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new ImageLoadException("Formato de imagen no compatible.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, false, false);
                int count;
                try { count = reader.getNumImages(true); } catch (IOException ignored) { count = 1; }
                count = Math.max(1, Math.min(count, MAX_FRAMES));
                List<BufferedImage> frames = new ArrayList<>(count);
                int[] delays = new int[count];
                int canvasWidth = 0;
                int canvasHeight = 0;
                try {
                    Node streamRoot = reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0");
                    Node logical = find(streamRoot, "LogicalScreenDescriptor");
                    canvasWidth = attribute(logical, "logicalScreenWidth", 0);
                    canvasHeight = attribute(logical, "logicalScreenHeight", 0);
                } catch (Exception ignored) {}
                validateDimensions(canvasWidth, canvasHeight);
                BufferedImage canvas = null;
                for (int index = 0; index < count; index++) {
                    validateDimensions(reader.getWidth(index), reader.getHeight(index));
                    BufferedImage rawFrame = reader.read(index);
                    if (rawFrame == null) continue;
                    Node metadata = null;
                    try { metadata = reader.getImageMetadata(index).getAsTree("javax_imageio_gif_image_1.0"); } catch (Exception ignored) {}
                    Node descriptor = find(metadata, "ImageDescriptor");
                    Node control = find(metadata, "GraphicControlExtension");
                    int left = attribute(descriptor, "imageLeftPosition", 0);
                    int top = attribute(descriptor, "imageTopPosition", 0);
                    int frameWidth = attribute(descriptor, "imageWidth", rawFrame.getWidth());
                    int frameHeight = attribute(descriptor, "imageHeight", rawFrame.getHeight());
                    if (canvas == null) {
                        canvasWidth = Math.max(canvasWidth, left + Math.max(frameWidth, rawFrame.getWidth()));
                        canvasHeight = Math.max(canvasHeight, top + Math.max(frameHeight, rawFrame.getHeight()));
                        validateDimensions(canvasWidth, canvasHeight);
                        canvas = new BufferedImage(Math.max(1, canvasWidth), Math.max(1, canvasHeight), BufferedImage.TYPE_INT_ARGB);
                    }
                    String disposal = attribute(control, "disposalMethod", "none");
                    BufferedImage previous = "restoreToPrevious".equals(disposal) ? copy(canvas) : null;
                    Graphics2D composite = canvas.createGraphics();
                    composite.setComposite(AlphaComposite.SrcOver);
                    int drawX = rawFrame.getWidth() == canvas.getWidth() ? 0 : left;
                    int drawY = rawFrame.getHeight() == canvas.getHeight() ? 0 : top;
                    composite.drawImage(rawFrame, drawX, drawY, null);
                    composite.dispose();
                    frames.add(resize(copy(canvas), count > 1 ? MAX_ANIMATED_SIDE : MAX_TEXTURE_SIDE));
                    delays[frames.size() - 1] = Math.max(50, gifDelay(control));

                    if ("restoreToBackgroundColor".equals(disposal)) {
                        Graphics2D clear = canvas.createGraphics();
                        clear.setComposite(AlphaComposite.Clear);
                        clear.fillRect(left, top, frameWidth, frameHeight);
                        clear.dispose();
                    } else if (previous != null) {
                        canvas = previous;
                    }
                }
                if (frames.isEmpty()) throw new ImageLoadException("La imagen no contiene fotogramas validos.");
                if (frames.size() != delays.length) {
                    int[] trimmed = new int[frames.size()];
                    System.arraycopy(delays, 0, trimmed, 0, frames.size());
                    delays = trimmed;
                }
                return new DecodedImage(List.copyOf(frames), delays, Integer.toHexString(java.util.Arrays.hashCode(bytes)));
            } finally {
                reader.dispose();
            }
        }
    }

    private static BufferedImage resize(BufferedImage source) {
        return resize(source, MAX_TEXTURE_SIDE);
    }

    private static void validateDimensions(int width, int height) {
        if (width < 0 || height < 0 || (long)width * (long)height > MAX_DECODE_PIXELS) {
            throw new ImageLoadException("La imagen supera el limite seguro de 16 megapixeles antes de escalarse.");
        }
    }

    private static BufferedImage resize(BufferedImage source, int maximumSide) {
        int longest = Math.max(source.getWidth(), source.getHeight());
        if (longest <= maximumSide) return toArgb(source);
        double scale = (double)maximumSide / longest;
        int width = Math.max(1, (int)Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int)Math.round(source.getHeight() * scale));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private static BufferedImage toArgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) return source;
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    private static int gifDelay(Node control) {
        try {
            if (control != null) {
                NamedNodeMap attributes = control.getAttributes();
                Node delay = attributes.getNamedItem("delayTime");
                if (delay != null) return Integer.parseInt(delay.getNodeValue()) * 10;
            }
        } catch (Exception ignored) {}
        return 100;
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return result;
    }

    private static int attribute(Node node, String name, int fallback) {
        try {
            Node value = node.getAttributes().getNamedItem(name);
            return value == null ? fallback : Integer.parseInt(value.getNodeValue());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String attribute(Node node, String name, String fallback) {
        try {
            Node value = node.getAttributes().getNamedItem(name);
            return value == null ? fallback : value.getNodeValue();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static Node find(Node node, String name) {
        if (node == null) return null;
        if (name.equals(node.getNodeName())) return node;
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            Node found = find(child, name);
            if (found != null) return found;
        }
        return null;
    }

    static record DecodedImage(List<BufferedImage> frames, int[] delaysMs, String signature) {}

    static final class RenderedAnimation {
        private final List<Model> frames;
        private final int[] delaysMs;
        private final String signature;
        private final String error;

        private RenderedAnimation(List<Model> frames, int[] delaysMs, String signature, String error) {
            this.frames = frames;
            this.delaysMs = delaysMs;
            this.signature = signature;
            this.error = error;
        }

        static RenderedAnimation success(List<Model> frames, int[] delaysMs, String signature) {
            return new RenderedAnimation(frames, delaysMs, signature, "");
        }

        static RenderedAnimation failure(String error) {
            return new RenderedAnimation(List.of(), new int[0], "", error == null ? "No se pudo cargar la imagen." : error);
        }

        boolean success() { return !frames.isEmpty(); }
        Model frame(int index) { return frames.get(Math.floorMod(index, frames.size())); }
        int frameCount() { return frames.size(); }
        int delay(int index) { return delaysMs[Math.floorMod(index, delaysMs.length)]; }
        String signature() { return signature; }
        String error() { return error; }
    }

    private static final class ImageLoadException extends RuntimeException {
        ImageLoadException(String message) { super(message); }
        ImageLoadException(String message, Throwable cause) { super(message, cause); }
    }
}
