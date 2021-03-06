boolean run(Player player, ItemStack item, int slot){
	if(item.getWeapon() == null){
		player.getCheats().log(10, "Attempted to wear an item which has no equipment slot.");
		return true;
	}
	
	Container equip = player.getEquipment();
	Container inv = player.getInventory();
	
	WieldType target = item.getWeapon().getSlot();
	ItemStack old = equip.get(target.getSlot());
	
	if(old != null){
		if(old.matches(item)){
			if(old.getAmount() < old.getStackSize()){
				//Add the stacks together 
				
				if(old.getAmount() + item.getAmount() > old.getStackSize()){
					//Not all of the items can be added to the slot, but some can->
					int swap = old.getStackSize() - old.getAmount();
					old = old.setAmount(old.getStackSize());
					item = item.setAmount(item.getAmount() - swap);
					
					inv.set(slot, item);
					equip.set(target.getSlot(), old);
				}
				else{
					//All of the items can be added to the slot->
					inv.set(slot, null);
					equip.set(target.getSlot(), old.setAmount(old.getAmount() + item.getAmount()));
				}
			}
			else{
				//We're already at the max stack size->
				//Nothing will be accomplished by equipping this->
				self.yield();
				return true;
			}
		}
		else{
			//The two items do not match-> Remove the old one, equip the new one->
			inv.set(slot, old);
			equip.set(target.getSlot(), item);
		}
	}
	else{
		//There is currently no other item equipped in the slot->
		inv.set(slot, null);
		equip.set(target.getSlot(), item);
	}
	self.yield();
	return true;
}