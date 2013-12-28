package appeng.client.render;

import java.util.HashMap;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPart;
import appeng.api.parts.IFacadePart;
import appeng.client.ClientHelper;
import appeng.facade.IFacadeItem;
import appeng.util.Platform;

public class BusRenderer implements IItemRenderer
{

	public static final BusRenderer instance = new BusRenderer();

	public RenderBlocksWorkaround renderer = new RenderBlocksWorkaround();
	public static final HashMap<Integer, IPart> renderPart = new HashMap();

	public IPart getRenderer(ItemStack is, IPartItem c)
	{
		int id = (is.getItem().itemID << Platform.DEF_OFFSET) | is.getItemDamage();

		IPart part = renderPart.get( id );
		if ( part == null )
		{
			part = c.createPartFromItemStack( is );
			renderPart.put( id, part );
		}

		return part;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();
		if ( type == ItemRenderType.ENTITY )
			GL11.glTranslatef( -0.5f, -0.5f, -0.5f );
		if ( type == ItemRenderType.INVENTORY )
			GL11.glTranslatef( 0.0f, -0.1f, 0.0f );

		GL11.glTranslated( 0.2, 0.3, 0.0 );

		GL11.glColor4f( 1, 1, 1, 1 );
		Tessellator.instance.setColorOpaque_F( 1, 1, 1 );

		BusRenderHelper.instance.setBounds( 0, 0, 0, 1, 1, 1 );
		BusRenderHelper.instance.setTexture( null );
		BusRenderHelper.instance.setInvColor( 0xffffff );
		renderer.blockAccess = ClientHelper.proxy.getWorld();

		if ( item.getItem() instanceof IFacadeItem )
		{
			IFacadeItem fi = (IFacadeItem) item.getItem();
			IFacadePart fp = fi.createPartFromItemStack( item, ForgeDirection.SOUTH );

			if ( fp != null )
				fp.renderInventory( BusRenderHelper.instance, renderer );
		}
		else
			getRenderer( item, (IPartItem) item.getItem() ).renderInventory( BusRenderHelper.instance, renderer );

		GL11.glPopMatrix();
	}
}