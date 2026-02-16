package com.surface.events;

import com.cubk.event.impl.CancellableEvent;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public final class EventBlockRenderSide extends CancellableEvent {
	private final Block block;
	private final BlockPos blockPos;
	private final EnumFacing side;
	private final int id;

	private boolean doRender = true;

	public EventBlockRenderSide(Block block, BlockPos blockPos, EnumFacing side) {
		this.block = block;
		this.blockPos = blockPos;
		this.side = side;
		this.id = Block.getIdFromBlock(block);
	}

	public Block getBlock() {
		return block;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public EnumFacing getSide() {
		return side;
	}

	public int getID() {
		return id;
	}

	public boolean isDoRender() {
		return doRender;
	}

	public void setRender(boolean doRender) {
		this.doRender = doRender;
	}
}

