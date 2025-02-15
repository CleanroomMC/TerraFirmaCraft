/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.devices;

import java.util.Locale;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blockentities.ComposterBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;

public class TFCComposterBlock extends ExtendedBlock implements EntityBlockExtension
{
    public static final IntegerProperty STAGE = TFCBlockStateProperties.STAGE_8;
    public static final EnumProperty<CompostType> TYPE = TFCBlockStateProperties.COMPOST_TYPE;

    private static final VoxelShape[] SHAPES = Util.make(new VoxelShape[9], shapes -> {
        shapes[0] = Block.box(1D, 1D, 1D, 15D, 16D, 15D);
        for(int i = 1; i < 9; ++i)
        {
            shapes[i] = Shapes.join(Shapes.block(), Block.box(1.0D, Math.max(2, i * 2), 1.0D, 15.0D, 16.0D, 15.0D), BooleanOp.ONLY_FIRST);
        }
    });

    public TFCComposterBlock(ExtendedProperties properties)
    {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(STAGE, 0).setValue(TYPE, CompostType.NORMAL));
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        return level.getBlockEntity(pos, TFCBlockEntities.COMPOSTER.get()).map(composter -> composter.use(player.getItemInHand(hand), player, level.isClientSide)).orElse(InteractionResult.PASS);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        level.getBlockEntity(pos, TFCBlockEntities.COMPOSTER.get()).ifPresent(ComposterBlockEntity::randomTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random)
    {
        final CompostType type = state.getValue(TYPE);
        if (type == CompostType.NORMAL) return;
        SimpleParticleType particle = type == CompostType.READY ? TFCParticles.COMPOST_READY.get().getType() : TFCParticles.COMPOST_ROTTEN.get().getType();

        final double x = pos.getX() + random.nextDouble();
        final double y = pos.getY() + 1 + random.nextDouble() / 5D;
        final double z = pos.getZ() + random.nextDouble();
        final int count = Mth.nextInt(random, 0, 4);
        for (int i = 0; i < count; i++)
        {
            level.addParticle(particle, x, y, z, 0D, 0D, 0D);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES[state.getValue(STAGE)];
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        return state.canSurvive(level, currentPos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(STAGE, TYPE));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        level.getBlockEntity(pos, TFCBlockEntities.COMPOSTER.get()).ifPresent(TickCounterBlockEntity::resetCounter);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return state.getValue(TYPE) != CompostType.ROTTEN;
    }

    public enum CompostType implements StringRepresentable
    {
        NORMAL, READY, ROTTEN;

        private final String serializedName;

        CompostType()
        {
            this.serializedName = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName()
        {
            return serializedName;
        }
    }
}
