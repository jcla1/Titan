package org.maxgamer.rs.model.item;

import java.util.HashMap;

import org.maxgamer.rs.model.item.weapon.Weapon;
import org.maxgamer.rs.structure.YMLSerializable;
import org.maxgamer.structure.configs.ConfigSection;

/**
 * @author netherfoam
 */
public class ItemStack implements Comparable<ItemStack>, YMLSerializable {
	private static HashMap<String, ItemStack> cache = new HashMap<String, ItemStack>();
	/** The generic currency in the game */
	public static final ItemStack COINS = ItemStack.create(995);
	
	final private int id;
	final private long amount;
	final private int health;
	
	protected ItemStack(int id, long amount, int health) {
		this.id = id;
		this.amount = amount;
		this.health = health;
		if (id < 0) {
			throw new RuntimeException("Item ID may not be <= 0. Given ID #" + id);
		}
		if (id >= 0 && getDefinition() == null) {
			throw new RuntimeException("No such item definition exists for Item with ID #" + id + "!");
		}
		if (amount < 0) {
			throw new RuntimeException("ID > 0 and Amount < 0 is an invalid item state. Given ID: " + id + " amount: " + amount);
		}
	}
	
	public static ItemStack create(ConfigSection s) {
		return ItemStack.create(s.getInt("id"), s.getLong("amount", 1), s.getInt("health", 0));
	}
	
	/**
	 * Fetches a reference for an item with the given id, amount and health.
	 * This uses a reference pool for items, so that memory is saved where
	 * possible.
	 * @param id The ID
	 * @param amount the amount
	 * @return The itemstack.
	 */
	public static ItemStack create(int id, long amount, int health) {
		if (amount <= 0) {
			return null;
		}
		
		ItemStack cached = cache.get(id + "-" + amount + "-" + health);
		if (cached == null) {
			cached = new ItemStack(id, amount, health);
			cache.put(id + "-" + amount + "-" + health, cached);
		}
		return cached;
	}
	
	/**
	 * Effecitvely returns create(ID, 0, health), but skips the safety check to
	 * ensure that the amount is > 0. This creats an itemstack where getAmount()
	 * == 0 but the itemstack is not null like the return value of create().
	 * @param id the id of the item
	 * @param health the health of the item
	 * @return an itemstack representing the item with an amount of 0.
	 */
	public static ItemStack createEmpty(int id, int health) {
		int amount = 0;
		ItemStack cached = cache.get(id + "-" + amount + "-" + health);
		if (cached == null) {
			cached = new ItemStack(id, amount, health);
			cache.put(id + "-" + amount + "-" + health, cached);
		}
		return cached;
	}
	
	public static ItemStack create(int id, long amount) {
		return ItemStack.create(id, amount, 0);
	}
	
	public static ItemStack create(int id) {
		return ItemStack.create(id, 1);
	}
	
	public ItemProto getDefinition() {
		ItemProto proto = ItemProto.getDefinition(id);
		
		return proto;
	}
	
	public Weapon getWeapon() {
		return getDefinition().getWeapon();
	}
	
	public boolean isNoteable() {
		ItemStack noted = this.getNoted();
		return noted != this;
	}
	
	public ItemStack setAmount(long amount) {
		return ItemStack.create(this.id, amount, this.health);
	}
	
	public ItemStack getNoted() {
		ItemStack noted = ItemStack.create(this.getId() + 1, this.getAmount(), this.getHealth());
		if (noted.getDefinition().isNoted()) {
			return noted;
		}
		
		return this; //Not noteable.
	}
	
	public ItemStack getUnnoted() {
		if (this.getId() == 10828) {
			return ItemStack.create(10843, this.getAmount(), this.getHealth());
		}
		else {
			ItemStack item = ItemStack.create(this.getId() - 1, this.getAmount(), this.getHealth());
			if (item.getName() != null && item.getName().equals(this.getName())) return item;
			return this;
		}
	}
	
	public long getAmount() {
		return amount;
	}
	
	public int getHealth() {
		return (int) health;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean matches(ItemStack i) {
		if (i == null) return false;
		return (i.id == this.id || getDefinition().stacksWith(i.id) || i.getDefinition().stacksWith(this.id)) && i.health == this.health;
	}
	
	@Override
	public String toString() {
		return getDefinition().getName() + "(" + getId() + ") x" + getAmount();
	}
	
	@Override
	public int compareTo(ItemStack i) {
		long n = i.getId() - this.getId();
		if (n != 0) {
			return (int) n;
		}
		n = i.getAmount() - this.getAmount();
		if (n != 0) {
			return (int) Math.min(n, Integer.MAX_VALUE);
		}
		n = i.getHealth() - this.getHealth();
		return (int) Math.min(n, Integer.MAX_VALUE);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof ItemStack == false) return false;
		ItemStack i = (ItemStack) o;
		if (i.id != this.id) return false;
		if (i.amount != this.amount) return false;
		if (i.health != this.health) return false;
		
		return true;
	}
	
	public String getName() {
		return getDefinition().getName();
	}
	
	public String getExamine() {
		return getDefinition().getName();
	}
	
	public long getStackSize() {
		return getDefinition().getMaxStack();
	}
	
	@Override
	public ConfigSection serialize() {
		ConfigSection s = new ConfigSection();
		s.set("id", this.id);
		s.set("amount", this.amount);
		if (this.health != 0) s.set("health", this.health);
		return s;
	}
	
	@Override
	public void deserialize(ConfigSection map) {
		throw new RuntimeException("ItemStacks must be deserialized with the constructor.");
	}
	
	/**
	 * the model used for rendering this equipment piece
	 * @return
	 */
	/*
	 * public int getEquipId(boolean male) { ItemProto p = getDefinition();
	 * 
	 * int wornId; if(male) wornId = p.maleWornModelId2; else wornId =
	 * p.femaleWornModelId2; //if(male) return getDefinition().maleWornModelId1;
	 * //else return getDefinition().femaleWornModelId1;
	 * System.out.println(getDefinition().getId() + ": " + getName() +
	 * " worn ID: " + wornId); System.out.println(getDefinition().toString());
	 * return wornId; }
	 */
	
	public String[] getInventoryOptions() {
		return getDefinition().getInventoryOptions();
	}
	
	public String[] getGroundOptions() {
		return getDefinition().getGroundOptions();
	}
	
	public boolean isNoted() {
		return getDefinition().isNoted();
	}
}
