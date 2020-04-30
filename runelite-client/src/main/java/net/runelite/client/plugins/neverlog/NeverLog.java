// private final Client client;
/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.neverlog;

import com.google.gson.Gson;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;


import net.runelite.api.GrandExchangeOffer;

@PluginDescriptor(
        name = "Never Log",
        description = "Enable this and you will never log out"
        )
@SuppressWarnings("unused")
public class NeverLog extends Plugin
{
@Inject
private Client client;

int timer = 0;
private Random random = new Random();
private long randomDelay;

@Override
protected void startUp()
{
        randomDelay = randomDelay();
}

@Override
protected void shutDown()
{

}
@Subscribe
public void onGameTick(GameTick event)
{
        timer++;
//		System.out.println("Ticked");
        if (timer > 10) {
//		Executors.newSingleThreadExecutor().submit(this::pressKey);
                this.getInventory();
                // this.getGEOffers();
                timer = 0;
        }

        // if (checkIdleLogout())
        // {
        //  randomDelay = randomDelay();
        //  Executors.newSingleThreadExecutor()
        //    .submit(this::pressKey);
        // }
}

private boolean checkIdleLogout()
{
        int idleClientTicks = client.getKeyboardIdleTicks();

        if (client.getMouseIdleTicks() < idleClientTicks)
        {
                idleClientTicks = client.getMouseIdleTicks();
        }

        return idleClientTicks >= randomDelay;
}

private long randomDelay()
{
        return (long) clamp(
                Math.round(random.nextGaussian() * 800)
                );
}

private static double clamp(double val)
{
        return Math.max(1, Math.min(13000, val));
}

private void getInventory()
{
        final int INVENTORY_SIZE = 28;
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);

        for (int i = 0; i < INVENTORY_SIZE; i++)
        {
                Item item = itemContainer.getItem(i);
                if (item == null) {
                        continue;
                }
                if (item.getQuantity() > 0)
                {
                        System.out.println("ID: " + item.id);
                        System.out.println("Quantity: " + item.getQuantity());
                        System.out.println("Slot: " + i);
                        System.out.println("");
                } else {
                        System.out.println("No item found!");
                }
        }


        // KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE);
        // this.client.getCanvas().dispatchEvent(keyPress);
        // KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE);
        // this.client.getCanvas().dispatchEvent(keyRelease);
        // KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE);
        // this.client.getCanvas().dispatchEvent(keyTyped);
}

private void getGEOffers() {
        GrandExchangeOffer[] geOffers = client.getGrandExchangeOffers();



        // boolean buying = newOffer.getState() == GrandExchangeOfferState.BOUGHT
        //                  || newOffer.getState() == GrandExchangeOfferState.BUYING
        //                  || newOffer.getState() == GrandExchangeOfferState.CANCELLED_BUY;
        // String offerState = (buying ? "Bought " : "Sold ")
        //                     + QuantityFormatter.quantityToRSDecimalStack(newOffer.getQuantitySold()) + " / "
        //                     + QuantityFormatter.quantityToRSDecimalStack(newOffer.getTotalQuantity());
        //
        // offerInfo.setText(offerState);
        //
        // itemPrice.setText(htmlLabel("Price each: ", QuantityFormatter.formatNumber(newOffer.getPrice())));
        //
        // String action = buying ? "Spent: " : "Received: ";
        //
        // offerSpent.setText(htmlLabel(action, QuantityFormatter.formatNumber(newOffer.getSpent()) + " / "
        //                              + QuantityFormatter.formatNumber(newOffer.getPrice() * newOffer.getTotalQuantity())));
}
}
