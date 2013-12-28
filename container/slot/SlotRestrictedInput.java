package appeng.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.crafting.ICraftingPatternMAC;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.ISpatialStorageCell;
import appeng.api.implementations.IStorageComponent;
import appeng.util.Platform;

public class SlotRestrictedInput extends AppEngSlot
{

	public enum PlaceableItemType
	{
		STORAGE_CELLS(15), ORE(1 * 16 + 15), STORAGE_COMPONENT(3 * 16 + 15), WIRELESS_TERMINAL(4 * 16 + 15), TRASH(5 * 16 + 15), VALID_ENCODED_PATTERN_W_OUPUT(7 * 16 + 15), ENCODED_PATTERN_W_OUTPUT(
				7 * 16 + 15), ENCODED_PATTERN(7 * 16 + 15), BLANK_PATTERN(8 * 16 + 15), POWERED_TOOL(9 * 16 + 15), RANGE_BOOSTER(6 * 16 + 15), QE_SINGULARTIY(10 * 16 + 15), SPATIAL_STORAGE_CELLS(
				11 * 16 + 15), FUEL(12 * 16 + 15);

		public final int icon;

		private PlaceableItemType(int o) {
			icon = o;
		}
	};

	@Override
	public int getSlotStackLimit()
	{
		if ( stackLimit != -1 )
			return stackLimit;
		return super.getSlotStackLimit();
	}

	public boolean isValid(ItemStack is, World theWorld)
	{
		if ( which == PlaceableItemType.VALID_ENCODED_PATTERN_W_OUPUT )
		{
			ICraftingPatternMAC ap = is.getItem() instanceof ICraftingPatternItem ? ((ICraftingPatternItem) is.getItem()).getPatternForItem( is ) : null;
			if ( ap != null && ap.isEncoded() && ap.isCraftable( theWorld ) )
				return true;
			return false;
		}
		return true;
	}

	public PlaceableItemType which;
	public int stackLimit = -1;

	public Slot setStackLimit(int i)
	{
		stackLimit = i;
		return this;
	}

	public SlotRestrictedInput(PlaceableItemType valid, IInventory i, int slotnum, int x, int y) {
		super( i, slotnum, x, y );
		which = valid;
		icon = valid.icon;
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if ( Platform.isClient() && (which == PlaceableItemType.VALID_ENCODED_PATTERN_W_OUPUT || which == PlaceableItemType.ENCODED_PATTERN_W_OUTPUT) )
		{
			ItemStack is = super.getStack();
			if ( is != null )
			{
				ICraftingPatternMAC ap = is.getItem() instanceof ICraftingPatternItem ? ((ICraftingPatternItem) is.getItem()).getPatternForItem( is ) : null;
				if ( ap != null )
				{
					return ap.getOutput().getItemStack();
				}
			}
		}
		return super.getStack();
	}

	@Override
	public boolean isItemValid(ItemStack i)
	{
		if ( i == null )
			return false;
		if ( i.getItem() == null )
			return false;

		IAppEngApi api = AEApi.instance();
		switch (which)
		{
		case VALID_ENCODED_PATTERN_W_OUPUT:
		case ENCODED_PATTERN_W_OUTPUT:
		case ENCODED_PATTERN: {
			ICraftingPatternMAC pattern = i.getItem() instanceof ICraftingPatternItem ? ((ICraftingPatternItem) i.getItem()).getPatternForItem( i ) : null;
			if ( pattern != null )
				return pattern.isEncoded();
			return false;
		}
		case BLANK_PATTERN: {
			ICraftingPatternMAC pattern = i.getItem() instanceof ICraftingPatternItem ? ((ICraftingPatternItem) i.getItem()).getPatternForItem( i ) : null;
			if ( pattern != null )
				return !pattern.isEncoded();

			return false;
		}
		case ORE:
			return appeng.api.AEApi.instance().registries().grinder().getRecipeForInput( i ) != null;
		case FUEL:
			return TileEntityFurnace.getItemBurnTime( i ) > 0;
		case POWERED_TOOL:
			return Platform.isChargeable( i );
		case QE_SINGULARTIY:
			return api.materials().materialQESingularity.sameAs( i );
		case RANGE_BOOSTER:
			return api.materials().materialWirelessBooster.sameAs( i );
		case SPATIAL_STORAGE_CELLS:
			return i.getItem() instanceof ISpatialStorageCell && ((ISpatialStorageCell) i.getItem()).isSpatialStorage( i );
		case STORAGE_CELLS:
			return AEApi.instance().registries().cell().isCellHandled( i );
		case STORAGE_COMPONENT:
			boolean isComp = i.getItem() instanceof IStorageComponent && ((IStorageComponent) i.getItem()).isStorageComponent( i );
			return isComp;
		case TRASH:
			if ( AEApi.instance().registries().cell().isCellHandled( i ) )
				return false;
			if ( i.getItem() instanceof IStorageComponent && ((IStorageComponent) i.getItem()).isStorageComponent( i ) )
				return false;
			return true;
		case WIRELESS_TERMINAL:
			return AEApi.instance().registries().wireless().isWirelessTerminal( i );
		}

		return false;
	}
}