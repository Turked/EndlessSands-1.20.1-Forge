package net.MechGaming.EndlessSands.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TwigBlock extends Block {
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 3);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 3);
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 15);
    public static final BooleanProperty RANDOMIZED = BooleanProperty.create("randomized");

    private static final int[] OFFSETS = {-4, -1, 1, 4};
    private static final double BASE_MIN_X = 5.0D;
    private static final double BASE_MIN_Y = 0.0D;
    private static final double BASE_MIN_Z = 4.0D;
    private static final double BASE_MAX_X = 11.0D;
    private static final double BASE_MAX_Y = 2.0D;
    private static final double BASE_MAX_Z = 12.0D;
    private static final VoxelShape[] SHAPES = makeShapes();

    public TwigBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OFFSET_X, 1)
                .setValue(OFFSET_Z, 1)
                .setValue(ROTATION, 0)
                .setValue(RANDOMIZED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[shapeIndex(state)];
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = randomize(
                this.defaultBlockState(),
                RandomSource.create(placementSeed(context))
        );
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide && !oldState.is(this) && !state.getValue(RANDOMIZED)) {
            level.setBlock(
                    pos,
                    randomize(state, RandomSource.create(pos.asLong())),
                    Block.UPDATE_ALL
            );
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.below();
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack twig = new ItemStack(this.asItem());
        if (!player.addItem(twig)) {
            player.drop(twig, false);
        }

        level.removeBlock(pos, false);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Z, ROTATION, RANDOMIZED);
    }

    public static int offsetFor(int offsetIndex) {
        return OFFSETS[offsetIndex];
    }

    public static int offsetIndexFor(int offset) {
        for (int i = 0; i < OFFSETS.length; i++) {
            if (OFFSETS[i] == offset) {
                return i;
            }
        }

        return 1;
    }

    private static BlockState randomize(BlockState state, RandomSource random) {
        return state.setValue(OFFSET_X, random.nextInt(4))
                .setValue(OFFSET_Z, random.nextInt(4))
                .setValue(ROTATION, random.nextInt(16))
                .setValue(RANDOMIZED, true);
    }

    private static long placementSeed(BlockPlaceContext context) {
        long seed = context.getClickedPos().asLong();
        seed = seed * 31L + context.getClickedFace().ordinal();

        Player player = context.getPlayer();
        if (player != null) {
            seed ^= player.getUUID().getMostSignificantBits();
            seed ^= Long.rotateLeft(player.getUUID().getLeastSignificantBits(), 32);
        }

        return seed;
    }

    private static int shapeIndex(BlockState state) {
        return state.getValue(ROTATION) * 16
                + state.getValue(OFFSET_X) * 4
                + state.getValue(OFFSET_Z);
    }

    private static VoxelShape[] makeShapes() {
        VoxelShape[] shapes = new VoxelShape[256];

        for (int rotation = 0; rotation < 16; rotation++) {
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    shapes[rotation * 16 + x * 4 + z] = makeShape(rotation, OFFSETS[x], OFFSETS[z]);
                }
            }
        }

        return shapes;
    }

    private static VoxelShape makeShape(int rotation, int xOffset, int zOffset) {
        double[] a = rotate(BASE_MIN_X, BASE_MIN_Z, rotation);
        double[] b = rotate(BASE_MIN_X, BASE_MAX_Z, rotation);
        double[] c = rotate(BASE_MAX_X, BASE_MIN_Z, rotation);
        double[] d = rotate(BASE_MAX_X, BASE_MAX_Z, rotation);

        double minX = Math.min(Math.min(a[0], b[0]), Math.min(c[0], d[0])) + xOffset;
        double minZ = Math.min(Math.min(a[1], b[1]), Math.min(c[1], d[1])) + zOffset;
        double maxX = Math.max(Math.max(a[0], b[0]), Math.max(c[0], d[0])) + xOffset;
        double maxZ = Math.max(Math.max(a[1], b[1]), Math.max(c[1], d[1])) + zOffset;

        return Block.box(minX, BASE_MIN_Y, minZ, maxX, BASE_MAX_Y, maxZ);
    }

    private static double[] rotate(double x, double z, int rotation) {
        double relativeX = x - 8.0D;
        double relativeZ = z - 8.0D;
        double angle = Math.toRadians(rotation * 22.5D);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new double[]{
                8.0D + relativeX * cos + relativeZ * sin,
                8.0D - relativeX * sin + relativeZ * cos
        };
    }
}
