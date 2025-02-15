/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blocks.devices.BellowsBlock;
import net.dries007.tfc.common.blocks.devices.IBellowsConsumer;

public class BellowsBlockEntity extends TFCBlockEntity
{
    private static final List<BellowsOffset> OFFSETS = new ArrayList<>();
    private static final int BELLOWS_AIR = 200;

    static
    {
        addBellowsOffset(new BellowsOffset(1, 0, 0, state -> state.getValue(BellowsBlock.FACING).getOpposite()));
        addBellowsOffset(new BellowsOffset(1, -1, 0, Direction.UP));
    }

    public static void addBellowsOffset(BellowsOffset offset)
    {
        OFFSETS.add(offset);
    }

    private long lastPushed = 0L;

    public BellowsBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCBlockEntities.BELLOWS.get(), pos, state);
    }

    public float getExtensionLength()
    {
        int time = (int) (level.getGameTime() - lastPushed);
        if (time < 10)
        {
            return time * 0.05f + 0.125f;
        }
        else if (time < 20)
        {
            return (20 - time) * 0.05f + 0.125f;
        }
        return 0.125f;
    }

    public InteractionResult onRightClick()
    {
        if (level.getGameTime() - lastPushed < 20) return InteractionResult.FAIL;
        level.playSound(null, worldPosition, TFCSounds.BELLOWS.get(), SoundSource.BLOCKS, 1, 1 + ((level.random.nextFloat() - level.random.nextFloat()) / 16));
        lastPushed = level.getGameTime();

        Direction direction = getBlockState().getValue(BellowsBlock.FACING);
        BlockPos facingPos = worldPosition.relative(direction);

        level.addParticle(ParticleTypes.POOF, facingPos.getX() + 0.5f - 0.3f * direction.getStepX(), facingPos.getY() + 0.5f, facingPos.getZ() + 0.5f - 0.3f * direction.getStepZ(), 0, 0.005D, 0);

        for (BellowsOffset offset : OFFSETS)
        {
            Direction airDirection = offset.getDirection(getBlockState());
            BlockPos airPosition = worldPosition.above(offset.getY())
                .relative(direction, offset.getX())
                .relative(direction.getClockWise(), offset.getZ());

            BlockState state = level.getBlockState(airPosition);
            if (state.getBlock() instanceof IBellowsConsumer consumer)
            {
                if (consumer.canAcceptAir(state, level, airPosition, airDirection))
                {
                    consumer.intakeAir(state, level, airPosition, airDirection, BELLOWS_AIR);
                    return InteractionResult.SUCCESS;
                }
            }

        }
        return InteractionResult.SUCCESS;
    }

    /**
     * @param pos a
     */
    public record BellowsOffset(Vec3i pos, Function<BlockState, Direction> directionMapper)
    {
        public BellowsOffset(int x, int y, int z, Function<BlockState, Direction> mapper)
        {
            this(new Vec3i(x, y, z), mapper);
        }

        public BellowsOffset(Vec3i pos, Direction dir)
        {
            this(pos, s -> dir);
        }

        public BellowsOffset(int x, int y, int z, Direction dir)
        {
            this(x, y, z, s -> dir);
        }

        public Direction getDirection(BlockState state)
        {
            return directionMapper.apply(state);
        }

        public int getX()
        {
            return pos.getX();
        }

        public int getY()
        {
            return pos.getY();
        }

        public int getZ()
        {
            return pos.getZ();
        }
    }
}
