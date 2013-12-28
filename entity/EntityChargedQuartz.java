package appeng.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.client.render.effects.LightningEffect;
import appeng.core.Configuration;
import appeng.util.Platform;

final public class EntityChargedQuartz extends EntityItem
{

	int delay = 0;
	int transformTime = 0;

	public EntityChargedQuartz(World w) {
		super( w );
	}

	public EntityChargedQuartz(World w, double x, double y, double z, ItemStack is) {
		super( w, x, y, z, is );
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if ( Platform.isClient() && delay++ > 30 && Configuration.instance.enableEffects )
		{
			delay = 0;
			LightningEffect fx = new LightningEffect( worldObj, posX, posY + 0.3f, posZ, 0.0f, 0.0f, 0.0f );
			Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
		}

		int j = MathHelper.floor_double( this.posX );
		int i = MathHelper.floor_double( this.posY );
		int k = MathHelper.floor_double( this.posZ );

		Material mat = worldObj.getBlockMaterial( j, i, k );
		if ( Platform.isServer() && mat.isLiquid() )
		{
			transformTime++;
			if ( transformTime > 60 )
			{
				if ( !transform() )
					transformTime = 0;
			}
		}
		else
			transformTime = 0;
	};

	public boolean transform()
	{
		ItemStack item = getEntityItem();
		if ( AEApi.instance().materials().materialCertusQuartzCrystalCharged.sameAs( item ) )
		{
			AxisAlignedBB region = AxisAlignedBB.getBoundingBox( posX - 1, posY - 1, posZ - 1, posX + 1, posY + 1, posZ + 1 );
			List<Entity> l = worldObj.getEntitiesWithinAABBExcludingEntity( this, region );

			EntityItem redstone = null;
			EntityItem netherQuartz = null;

			for (Entity e : l)
			{
				if ( e instanceof EntityItem && !e.isDead )
				{
					ItemStack other = ((EntityItem) e).getEntityItem();
					if ( other != null && other.stackSize > 0 )
					{
						if ( Platform.isSameItem( other, new ItemStack( Item.redstone ) ) )
							redstone = (EntityItem) e;

						if ( Platform.isSameItem( other, new ItemStack( Item.netherQuartz ) ) )
							netherQuartz = (EntityItem) e;
					}
				}
			}

			if ( redstone != null && netherQuartz != null )
			{
				getEntityItem().stackSize--;
				redstone.getEntityItem().stackSize--;
				netherQuartz.getEntityItem().stackSize--;

				if ( getEntityItem().stackSize <= 0 )
					setDead();

				if ( redstone.getEntityItem().stackSize <= 0 )
					redstone.setDead();

				if ( netherQuartz.getEntityItem().stackSize <= 0 )
					netherQuartz.setDead();

				List<ItemStack> i = new ArrayList();
				i.add( AEApi.instance().materials().materialFluixCrystal.stack( 1 ) );

				ItemStack Output = AEApi.instance().materials().materialFluixCrystal.stack( 2 );
				worldObj.spawnEntityInWorld( new EntityItem( worldObj, posX, posY, posZ, Output ) );

				return true;
			}
		}
		return false;
	}
}