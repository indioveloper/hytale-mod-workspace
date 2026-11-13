import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.manual.ManualCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.BaseHeightDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CurveMapperDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.EllipsoidDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.AnchorDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MaxDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MinDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.InverterDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MultiplierDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.PlaneDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SimplexNoise3DDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SimplexNoise2dDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SumDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.VectorWarpDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.WhiteNoiseDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.PositionsCellNoiseDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions.DistanceFunctionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions.EuclideanDistanceFunctionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.DensityReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.ReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders.ConstantEnvironmentProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders.EnvironmentProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.FieldFunctionMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.QueueMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.SolidityMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.WeightedMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.SpaceAndDepthMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.ConstantThicknessLayerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.LayerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.BlockSetPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.FloorPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.MaterialPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.ConstantPropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.LocatorPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.ManualPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.WeightedPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.DirectScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.LinearScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.BoundPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.FieldFunctionOccurrencePositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.Jitter3dPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.OffsetPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ScalerPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.SquareGrid3dPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.UnionPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.terrains.DensityTerrainAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.terrains.TerrainAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.tintproviders.ConstantTintProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.tintproviders.TintProviderAsset;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;

import java.nio.file.Path;

/**
 * Small pre-release codec smoke test. It registers only the polymorphic node
 * types used by the baseline, avoiding any need to bootstrap a live server.
 */
public final class ValidateWorldGenV2Codec {
    private ValidateWorldGenV2Codec() {
    }

    public static void main(String[] args) throws Exception {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("Expected <biome.json>");
            }

            System.out.println("Registering baseline WorldGen V2 codecs...");
            registerBaselineCodecs();

            System.out.println("Decoding Biome...");
            BiomeAsset biome = decodeBiome(Path.of(args[0]));
            if (biome == null) {
                throw new IllegalStateException("Biome codec returned null for " + args[0]);
            }

            System.out.println("Pre-release HytaleGenerator codecs accepted the Biome asset.");
        } catch (Throwable error) {
            error.printStackTrace(System.err);
            throw error;
        }
    }

    private static BiomeAsset decodeBiome(Path path) throws Exception {
        try (var reader = RawJsonReader.fromPath(path, RawJsonReader.READ_BUFFER.get())) {
            return BiomeAsset.CODEC.decode(RawJsonReader.readBsonDocument(reader), new ExtraInfo());
        }
    }

    private static void registerBaselineCodecs() {
        TerrainAsset.CODEC.register("DAOTerrain", DensityTerrainAsset.class, DensityTerrainAsset.CODEC);

        DensityAsset.CODEC.register("Sum", SumDensityAsset.class, SumDensityAsset.CODEC);
        DensityAsset.CODEC.register("SimplexNoise2D", SimplexNoise2dDensityAsset.class, SimplexNoise2dDensityAsset.CODEC);
        DensityAsset.CODEC.register("CurveMapper", CurveMapperDensityAsset.class, CurveMapperDensityAsset.CODEC);
        DensityAsset.CODEC.register("BaseHeight", BaseHeightDensityAsset.class, BaseHeightDensityAsset.CODEC);
        DensityAsset.CODEC.register("Anchor", AnchorDensityAsset.class, AnchorDensityAsset.CODEC);
        DensityAsset.CODEC.register("Constant", ConstantDensityAsset.class, ConstantDensityAsset.CODEC);
        DensityAsset.CODEC.register("Ellipsoid", EllipsoidDensityAsset.class, EllipsoidDensityAsset.CODEC);
        DensityAsset.CODEC.register("Max", MaxDensityAsset.class, MaxDensityAsset.CODEC);
        DensityAsset.CODEC.register("Min", MinDensityAsset.class, MinDensityAsset.CODEC);
        DensityAsset.CODEC.register("Inverter", InverterDensityAsset.class, InverterDensityAsset.CODEC);
        DensityAsset.CODEC.register("Multiplier", MultiplierDensityAsset.class, MultiplierDensityAsset.CODEC);
        DensityAsset.CODEC.register("Plane", PlaneDensityAsset.class, PlaneDensityAsset.CODEC);
        DensityAsset.CODEC.register("PositionsCellNoise", PositionsCellNoiseDensityAsset.class, PositionsCellNoiseDensityAsset.CODEC);
        DensityAsset.CODEC.register("SimplexNoise3D", SimplexNoise3DDensityAsset.class, SimplexNoise3DDensityAsset.CODEC);
        DensityAsset.CODEC.register("WhiteNoise", WhiteNoiseDensityAsset.class, WhiteNoiseDensityAsset.CODEC);
        DensityAsset.CODEC.register("VectorWarp", VectorWarpDensityAsset.class, VectorWarpDensityAsset.CODEC);
        CurveAsset.CODEC.register("Manual", ManualCurveAsset.class, ManualCurveAsset.CODEC);

        PositionProviderAsset.CODEC.register("Jitter3d", Jitter3dPositionProviderAsset.class, Jitter3dPositionProviderAsset.CODEC);
        PositionProviderAsset.CODEC.register("Bound", BoundPositionProviderAsset.class, BoundPositionProviderAsset.CODEC);
        PositionProviderAsset.CODEC.register("Occurrence", FieldFunctionOccurrencePositionProviderAsset.class, FieldFunctionOccurrencePositionProviderAsset.CODEC);
        PositionProviderAsset.CODEC.register("List", ListPositionProviderAsset.class, ListPositionProviderAsset.CODEC);
        PositionProviderAsset.CODEC.register("Offset", OffsetPositionProviderAsset.class, OffsetPositionProviderAsset.CODEC);
        PositionProviderAsset.CODEC.register("Scaler", ScalerPositionProviderAsset.class, ScalerPositionProviderAsset.CODEC);
        PositionProviderAsset.CODEC.register("SquareGrid3d", SquareGrid3dPositionProviderAsset.class, SquareGrid3dPositionProviderAsset.CODEC);
        PositionProviderAsset.CODEC.register("Union", UnionPositionProviderAsset.class, UnionPositionProviderAsset.CODEC);
        ReturnTypeAsset.CODEC.register("Density", DensityReturnTypeAsset.class, DensityReturnTypeAsset.CODEC);
        DistanceFunctionAsset.CODEC.register("Euclidean", EuclideanDistanceFunctionAsset.class, EuclideanDistanceFunctionAsset.CODEC);

        MaterialProviderAsset.CODEC.register("Solidity", SolidityMaterialProviderAsset.class, SolidityMaterialProviderAsset.CODEC);
        MaterialProviderAsset.CODEC.register("Queue", QueueMaterialProviderAsset.class, QueueMaterialProviderAsset.CODEC);
        MaterialProviderAsset.CODEC.register("SpaceAndDepth", SpaceAndDepthMaterialProviderAsset.class, SpaceAndDepthMaterialProviderAsset.CODEC);
        MaterialProviderAsset.CODEC.register("Constant", ConstantMaterialProviderAsset.class, ConstantMaterialProviderAsset.CODEC);
        MaterialProviderAsset.CODEC.register("FieldFunction", FieldFunctionMaterialProviderAsset.class, FieldFunctionMaterialProviderAsset.CODEC);
        MaterialProviderAsset.CODEC.register("Weighted", WeightedMaterialProviderAsset.class, WeightedMaterialProviderAsset.CODEC);
        LayerAsset.CODEC.register("ConstantThickness", ConstantThicknessLayerAsset.class, ConstantThicknessLayerAsset.CODEC);

        PropDistributionAsset.CODEC.register("Constant", ConstantPropDistributionAsset.class, ConstantPropDistributionAsset.CODEC);
        PropAsset.CODEC.register("Locator", LocatorPropAsset.class, LocatorPropAsset.CODEC);
        PropAsset.CODEC.register("Manual", ManualPropAsset.class, ManualPropAsset.CODEC);
        PropAsset.CODEC.register("Weighted", WeightedPropAsset.class, WeightedPropAsset.CODEC);
        PatternAsset.CODEC.register("BlockSet", BlockSetPatternAsset.class, BlockSetPatternAsset.CODEC);
        PatternAsset.CODEC.register("BlockType", MaterialPatternAsset.class, MaterialPatternAsset.CODEC);
        PatternAsset.CODEC.register("Floor", FloorPatternAsset.class, FloorPatternAsset.CODEC);
        ScannerAsset.CODEC.register("Origin", DirectScannerAsset.class, DirectScannerAsset.CODEC);
        ScannerAsset.CODEC.register("Linear", LinearScannerAsset.class, LinearScannerAsset.CODEC);

        EnvironmentProviderAsset.CODEC.register("Constant", ConstantEnvironmentProviderAsset.class, ConstantEnvironmentProviderAsset.CODEC);
        TintProviderAsset.CODEC.register("Constant", ConstantTintProviderAsset.class, ConstantTintProviderAsset.CODEC);
    }
}
