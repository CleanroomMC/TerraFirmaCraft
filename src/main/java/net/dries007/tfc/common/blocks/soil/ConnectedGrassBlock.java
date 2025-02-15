/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.soil;

import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;

public class ConnectedGrassBlock extends Block implements IGrassBlock
{
    // Used to determine connected textures
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    private static final Map<Direction, BooleanProperty> PROPERTIES = ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.WEST, WEST, Direction.SOUTH, SOUTH);

    private final Supplier<? extends Block> dirt;
    @Nullable private final Supplier<? extends Block> path;

    public ConnectedGrassBlock(Properties properties, SoilBlockType dirtType, SoilBlockType.Variant soilType)
    {
        this(properties, TFCBlocks.SOIL.get(dirtType).get(soilType), TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(soilType));
    }

    public ConnectedGrassBlock(Properties properties, Supplier<? extends Block> dirt, @Nullable Supplier<? extends Block> path)
    {
        super(properties.hasPostProcess(TFCBlocks::always));

        this.dirt = dirt;
        this.path = path;

        registerDefaultState(stateDefinition.any().setValue(SOUTH, false).setValue(EAST, false).setValue(NORTH, false).setValue(WEST, false).setValue(SNOWY, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        if (facing == Direction.UP)
        {
            return stateIn.setValue(SNOWY, Helpers.isBlock(facingState, TFCTags.Blocks.SNOW));
        }
        else if (facing != Direction.DOWN)
        {
            return updateStateFromDirection(level, currentPos, stateIn, facing);
        }
        return stateIn;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        level.scheduleTick(pos, this, 0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            level.scheduleTick(pos.relative(direction).above(), this, 0);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            level.scheduleTick(pos.relative(direction).above(), this, 0);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        if (!canBeGrass(state, level, pos))
        {
            if (level.isAreaLoaded(pos, 3))
            {
                // Turn to not-grass
                level.setBlockAndUpdate(pos, getDirt());
            }
        }
        else
        {
            if (level.getMaxLocalRawBrightness(pos.above()) >= 9)
            {
                for (int i = 0; i < 4; ++i)
                {
                    BlockPos posAt = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    BlockState stateAt = level.getBlockState(posAt);
                    if (stateAt.getBlock() instanceof IDirtBlock dirt)
                    {
                        // Spread grass to others
                        BlockState grassState = dirt.getGrass();
                        if (canPropagate(grassState, level, posAt))
                        {
                            level.setBlockAndUpdate(posAt, updateStateFromNeighbors(level, posAt, grassState));
                        }
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand)
    {
        level.setBlock(pos, updateStateFromNeighbors(level, pos, state), 2);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState stateUp = context.getLevel().getBlockState(context.getClickedPos().above());
        return updateStateFromNeighbors(context.getLevel(), context.getClickedPos(), defaultBlockState()).setValue(SNOWY, Helpers.isBlock(stateUp, Blocks.SNOW_BLOCK) || Helpers.isBlock(stateUp, Blocks.SNOW));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, SOUTH, WEST, SNOWY);
    }

    @Override
    public BlockState getDirt()
    {
        return dirt.get().defaultBlockState();
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, Level world, BlockPos pos, Player player, ItemStack stack, ToolAction action)
    {
        if (stack.canPerformAction(action) && action == ToolActions.SHOVEL_FLATTEN && TFCConfig.SERVER.enableGrassPathCreation.get() && path != null)
        {
            return path.get().defaultBlockState();
        }
        return null;
    }

    /**
     * Update the state of a grass block from all horizontal directions
     *
     * @param worldIn The world
     * @param pos     The position of the grass block
     * @param state   The initial state
     * @return The updated state
     */
    protected BlockState updateStateFromNeighbors(BlockGetter worldIn, BlockPos pos, BlockState state)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            state = updateStateFromDirection(worldIn, pos, state, direction);
        }
        return state;
    }

    /**
     * Update the state of a grass block from the provided direction
     *
     * @param worldIn   The world
     * @param pos       The position of the grass block
     * @param stateIn   The state of the grass block
     * @param direction The direction in which to look for adjacent, diagonal grass blocks
     * @return The updated state
     */
    protected BlockState updateStateFromDirection(BlockGetter worldIn, BlockPos pos, BlockState stateIn, Direction direction)
    {
        return stateIn.setValue(PROPERTIES.get(direction), worldIn.getBlockState(pos.relative(direction).below()).getBlock() instanceof IGrassBlock);
    }
}