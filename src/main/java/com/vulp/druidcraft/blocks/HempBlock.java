package com.vulp.druidcraft.blocks;

import com.vulp.druidcraft.registry.BlockRegistry;
import com.vulp.druidcraft.registry.ItemRegistry;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.PlantType;

import java.util.Random;

public class HempBlock extends DynamicCropBlock implements IGrowable {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_0_3;

    public HempBlock(Properties properties) {
        super(properties);
    }

    @Override
    public IntegerProperty getAgeProperty () {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    public boolean isValidGround(BlockState state, IBlockReader world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        return (blockState.getBlock() instanceof FarmlandBlock || blockState == BlockRegistry.hemp_crop.getDefaultState().with(AGE, 3));
    }

    @Override
    public boolean canGrow(IBlockReader world, BlockPos pos, BlockState state, boolean isClient) {
        return (!isMaxAge(state)) || (!(world.getBlockState(pos.down()).getBlock() instanceof HempBlock)) && (!(world.getBlockState(pos.up()).getBlock() instanceof HempBlock));
    }

    @Override
    public void grow(ServerWorld serverWorld, Random random, BlockPos blockPos, BlockState state) {
        int i = this.getAge(state) + this.getBonemealAgeIncrease(serverWorld);
        int j = this.getMaxAge();
        if (i > j) {
            i = j;
        }

        if (this.getAge(state) != j) {
            serverWorld.setBlockState(blockPos, this.withAge(i), 2);
        }
        else if ((this.getAge(state) == j)) {
            BlockState up = serverWorld.getBlockState(blockPos.up());
            if (up.getMaterial().isReplaceable() || up.isAir(serverWorld, blockPos.up())) {
                serverWorld.setBlockState(blockPos.up(), this.getDefaultState());
            }
        }
    }


    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!isValidGround(state, world, pos)) {
            world.destroyBlock(pos, true);
        }

        boolean topBlockValid = (!(world.getBlockState(pos.down()).getBlock() instanceof HempBlock)) && (world.isAirBlock(pos.up()));

        if (!world.isAreaLoaded(pos, 1))
            return;
        if (world.getLightSubtracted(pos, 0) >= 9) {
            int i = this.getAge(state);
            if (i < this.getMaxAge()) {
                float f = getGrowthChance(this, world, pos);
                if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(world, pos, state, random.nextInt((int) (25.0F / f) + 1) == 0)) {
                    world.setBlockState(pos, this.withAge(i + 1), 2);
                    net.minecraftforge.common.ForgeHooks.onCropsGrowPost(world, pos, state);
                }
            }
            else {
                if ((topBlockValid) && (i == this.getMaxAge())) {
                    world.setBlockState(pos.up(), this.getDefaultState());
                }
            }
        }
        super.tick(state, world, pos, random);
    }

    protected static float getGrowthChance(Block blockIn, IBlockReader worldIn, BlockPos pos) {
        float f = 1.1F;
        BlockPos blockpos = pos.down();
        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                float f1 = 0.0F;
                BlockState blockstate = worldIn.getBlockState(blockpos.add(i, 0, j));
                if (blockstate.canSustainPlant(worldIn, blockpos.add(i, 0, j), net.minecraft.util.Direction.UP, (net.minecraftforge.common.IPlantable)blockIn)) {
                    f1 = 1.0F;
                    if (blockstate.isFertile(worldIn, blockpos.add(i, 0, j))) {
                        f1 = 3.0F;
                    }
                }

                if (i != 0 || j != 0) {
                    f1 /= 4.0F;
                }

                f += f1;
            }
        }

        BlockPos blockpos1 = pos.north();
        BlockPos blockpos2 = pos.south();
        BlockPos blockpos3 = pos.west();
        BlockPos blockpos4 = pos.east();
        boolean flag = blockIn == worldIn.getBlockState(blockpos3).getBlock() || blockIn == worldIn.getBlockState(blockpos4).getBlock();
        boolean flag1 = blockIn == worldIn.getBlockState(blockpos1).getBlock() || blockIn == worldIn.getBlockState(blockpos2).getBlock();
        if (flag && flag1) {
            f /= 2.0F;
        } else {
            boolean flag2 = blockIn == worldIn.getBlockState(blockpos3.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.south()).getBlock() || blockIn == worldIn.getBlockState(blockpos3.south()).getBlock();
            if (flag2) {
                f /= 2.0F;
            }
        }

        return f;
    }

    @Override
    protected IItemProvider getSeedsItem() {
        return ItemRegistry.hemp_seeds;
    }

    protected int getBonemealAgeIncrease(World worldIn) {
        return new Random().nextInt(1) + 1;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !isValidGround(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public PlantType getPlantType(IBlockReader world, BlockPos pos) {
        return PlantType.CROP;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext) {
        return Block.makeCuboidShape(4, 0, 4, 12.0d, 4.0d * (state.get(getAgeProperty()) + 1), 12.0d);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

}