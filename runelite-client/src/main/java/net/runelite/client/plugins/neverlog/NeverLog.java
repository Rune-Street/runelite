// private final Client client;
/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.neverlog;

import com.google.gson.Gson;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import javax.inject.Inject;
;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;


import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;

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
                Executors.newSingleThreadExecutor().submit(this::getInventory);
//                this.getInventory();
                Executors.newSingleThreadExecutor().submit(this::getGEOffers);

//                this.getGEOffers();
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

class ItemDetailed
{
  public int id;
  public int quantity;
  public int slot;

  public ItemDetailed() {
    this.id = -1;
    this.quantity = -1;
    this.slot = -1;
  }

  public ItemDetailed(Item item, int slot) {
    this.id = item.id;
    this.quantity = item.quantity;
    this.slot = slot;
  }
}

private void getInventory()
{
        final int INVENTORY_SIZE = 28;
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
        List<ItemDetailed> items = new ArrayList<ItemDetailed>();

        for (int slot = 0; slot < INVENTORY_SIZE; slot++)
        {
                Item item = itemContainer.getItem(slot);
                if (item == null) {
                        continue;
                }
                if (item.getQuantity() > 0)
                {
                        // System.out.println("ID: " + item.id);
                        // System.out.println("Quantity: " + item.quantity);
                        // System.out.println("Slot: " + i);
                        // System.out.println("");
                  items.add(new ItemDetailed(item, slot));
                } else {
                        System.out.println("No item found!");
                }
        }
        Gson gson = new Gson();
        JsonObject container = new JsonObject();
        container.addProperty("type", "inventory");
        JsonElement data = gson.toJsonTree(items);
        container.add("data", data);
        String json = gson.toJson(container);
        System.out.println(json);

}

class GrandExchangeOfferClass
{
  public int quantity;
  public int id;
  public int total_quantity;
  public int price;
  public int gold;
  public String offer_state;

  public GrandExchangeOfferClass() {
    this.quantity = -1;
    this.id = -1;
    this.total_quantity =  -1;
    this.price = -1;
    this.gold = -1;
    this.offer_state = "uninitialized";
  }

  public GrandExchangeOfferClass(GrandExchangeOffer offer) {
    this.quantity = offer.getQuantitySold();
    this.id = offer.getItemId();
    this.total_quantity = offer.getTotalQuantity();
    this.price = offer.getPrice();
    this.gold = offer.getSpent();
    switch(offer.getState()) {
      case EMPTY: this.offer_state = "empty";
                  break;
      case CANCELLED_BUY: this.offer_state = "cancelled_buy";
                  break;
      case CANCELLED_SELL: this.offer_state = "cancelled_sell";
                  break;
      case BUYING: this.offer_state = "buying";
                  break;
      case BOUGHT: this.offer_state = "bought";
                  break;
      case SELLING: this.offer_state = "selling";
                  break;
      case SOLD: this.offer_state = "sold";
                  break;
    }
  }
}

private void getGEOffers() {
        GrandExchangeOffer[] geOfferInterfaces = client.getGrandExchangeOffers();
//        GrandExchangeOfferClass[] geOffers = new GrandExchangeOfferClass(geOfferInterfaces.length);
        List<GrandExchangeOfferClass> geOffers = new ArrayList<GrandExchangeOfferClass>();
        for (GrandExchangeOffer offerInterface : geOfferInterfaces) {
//        for (int i = 0; i < geOfferInterfaces.length; i++) {
//            geOffers[i] = new GrandExchangeOfferClass(geOfferInterfaces[i]);
            geOffers.add(new GrandExchangeOfferClass(offerInterface));
        }

        Gson gson = new Gson();
        JsonObject container = new JsonObject();
        container.addProperty("type", "ge");
        JsonElement data = gson.toJsonTree(geOffers);
        container.add("data", data);
        String json = gson.toJson(container);
        System.out.println(json);



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
