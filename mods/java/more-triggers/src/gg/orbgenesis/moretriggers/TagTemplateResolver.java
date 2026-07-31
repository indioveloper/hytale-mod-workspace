package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joml.Vector3d;

final class TagTemplateResolver {
  private static final Pattern TAG_TOKEN = Pattern.compile("\\{([^{}]+)}");

  private TagTemplateResolver() {}

  static String resolve(
      TriggerContext context,
      String template,
      TagSource sourceMode,
      double radius) {
    if (context == null) {
      return template == null ? "" : template;
    }

    VolumeEntry source = context.getVolume();
    List<Map<String, String>> tagSources = new ArrayList<>();
    Vector3d eventPosition = context.getEventPosition();
    Comparator<VolumeEntry> nearestFirst =
        Comparator.comparingDouble(
                (VolumeEntry volume) ->
                    eventPosition != null
                        ? volume.getPosition().distanceSquared(eventPosition)
                        : 0.0D)
            .thenComparing(VolumeEntry::getId);

    TagSource effectiveMode = sourceMode != null ? sourceMode : TagSource.SELF;
    switch (effectiveMode) {
      case SELF -> {
        if (source != null) {
          tagSources.add(source.getRawTags());
        }
      }
      case EVENT -> {
        List<VolumeEntry> spatialVolumes = new ArrayList<>(context.getSpatialVolumes());
        spatialVolumes.removeIf(volume -> volume == null);
        spatialVolumes.sort(nearestFirst);
        addTagSources(tagSources, spatialVolumes);
      }
      case RADIUS -> {
        TriggerVolumeManager manager =
            context
                .getStore()
                .getResource(TriggerVolumesPlugin.get().getManagerResourceType());
        if (manager != null) {
          double safeRadius = Math.max(0.0D, radius);
          double radiusSquared = safeRadius * safeRadius;
          List<VolumeEntry> nearbyVolumes = new ArrayList<>(manager.getVolumes());
          nearbyVolumes.removeIf(
              volume ->
                  volume == null
                      || eventPosition == null
                      || volume.getPosition().distanceSquared(eventPosition) > radiusSquared);
          nearbyVolumes.sort(nearestFirst);
          addTagSources(tagSources, nearbyVolumes);
        }
      }
    }
    return resolve(template, tagSources);
  }

  private static void addTagSources(
      List<Map<String, String>> tagSources,
      List<VolumeEntry> volumes) {
    for (VolumeEntry volume : volumes) {
      tagSources.add(volume.getRawTags());
    }
  }

  static String resolve(String template, Map<String, String> tags) {
    return resolve(template, tags != null ? List.of(tags) : List.of());
  }

  static String resolve(String template, List<Map<String, String>> tagSources) {
    if (template == null || template.isEmpty()) {
      return template == null ? "" : template;
    }

    Matcher matcher = TAG_TOKEN.matcher(template);
    StringBuffer resolved = new StringBuffer(template.length());
    while (matcher.find()) {
      String value = findValue(matcher.group(1), tagSources);
      if (value == null) {
        matcher.appendReplacement(resolved, Matcher.quoteReplacement(matcher.group()));
      } else {
        matcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
      }
    }
    matcher.appendTail(resolved);
    return resolved.toString();
  }

  private static String findValue(String key, List<Map<String, String>> tagSources) {
    if (tagSources == null) {
      return null;
    }
    for (Map<String, String> tags : tagSources) {
      if (tags != null && tags.containsKey(key)) {
        return tags.get(key);
      }
    }
    return null;
  }
}
