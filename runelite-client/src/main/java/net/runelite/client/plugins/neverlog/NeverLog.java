// private final Client client;
/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.neverlog;

import com.google.gson.Gson;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class NeverLog extends Plugin {
    @Inject
    private Client client;

    int timer = 0;
    private Random random = new Random();
    private long randomDelay;

    @Override
    protected void startUp() {
        randomDelay = randomDelay();
    }

    @Override
    protected void shutDown() {

    }

    @Subscribe
    public void onGameTick(GameTick event) {
      timer++;
      if (timer > 3) {
          // Executors.newSingleThreadExecutor().submit(this::getInventory);
          // Executors.newSingleThreadExecutor().submit(this::getGEOffers);
          this.getInventory();
          this.getGEOffers();
          timer = 0;
      }

        // if (checkIdleLogout())
        // {
        //  randomDelay = randomDelay();
        //  Executors.newSingleThreadExecutor()
        //    .submit(this::pressKey);
        // }
    }

    private boolean checkIdleLogout() {
        int idleClientTicks = client.getKeyboardIdleTicks();

        if (client.getMouseIdleTicks() < idleClientTicks) {
            idleClientTicks = client.getMouseIdleTicks();
        }

        return idleClientTicks >= randomDelay;
    }

    private long randomDelay() {
        return (long) clamp(
                Math.round(random.nextGaussian() * 800)
        );
    }

    private static double clamp(double val) {
        return Math.max(1, Math.min(13000, val));
    }

    class ItemDetailed {
        public int id;
        public int quantity;
        public String state;

        public ItemDetailed() {
            this.id = -1;
            this.quantity = -1;
            // this.slot = -1;
            this.state = "EMPTY";
        }

        public ItemDetailed(String state) {
            this.id = -1;
            this.quantity = -1;
            // this.slot = slot;
            this.state = state;
        }

        public ItemDetailed(Item item) {
            this.id = item.id;
            this.quantity = item.quantity;
            // this.slot = slot;
            this.state = "OCCUPIED";
        }
    }

    private void getInventory() {
        final int INVENTORY_SIZE = 28;
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
         List<ItemDetailed> items = new ArrayList<ItemDetailed>();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            items.add(new ItemDetailed());
        }

        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            Item item = itemContainer.getItem(slot);
            if (item == null) {
                continue;
            }
            if (item.getQuantity() > 0) {
              items.set(slot, new ItemDetailed(item));
            } else {
                items.set(slot, new ItemDetailed("ERROR"));
            }

        }
        Gson gson = new Gson();
        JsonObject container = new JsonObject();
        container.addProperty("type", "inventory");
        JsonElement data = gson.toJsonTree(items);
        container.add("data", data);
        String json = gson.toJson(container);
        // System.out.println(json);

        try {
            Files.write(Paths.get("/tmp/runelite.fifo"), (json + System.lineSeparator()).getBytes());
        } catch (IOException e) {
            System.out.println("Couldn't write to fifo!");
        }
    }

    class GrandExchangeOfferClass {
        public int id;
        public int quantity;
        public int total_quantity;
        public int price;
        public int gold;
        public String offer_state;

        public GrandExchangeOfferClass() {
            this.id = -1;
            this.quantity = -1;
            this.total_quantity = -1;
            this.price = -1;
            this.gold = -1;
            this.offer_state = "UNITIALIZED";
        }

        public GrandExchangeOfferClass(GrandExchangeOffer offer) {
            this.id = offer.getItemId();
            this.quantity = offer.getQuantitySold();
            this.total_quantity = offer.getTotalQuantity();
            this.price = offer.getPrice();
            this.gold = offer.getSpent();
            switch (offer.getState()) {
                case EMPTY:
                    this.offer_state = "EMPTY";
                    break;
                case CANCELLED_BUY:
                    this.offer_state = "CANCELLED_BUY";
                    break;
                case CANCELLED_SELL:
                    this.offer_state = "CANCELLED_SELL";
                    break;
                case BUYING:
                    this.offer_state = "BUYING";
                    break;
                case BOUGHT:
                    this.offer_state = "BOUGHT";
                    break;
                case SELLING:
                    this.offer_state = "SELLING";
                    break;
                case SOLD:
                    this.offer_state = "SOLD";
                    break;
            }
        }
    }

    private void getGEOffers() {
        GrandExchangeOffer[] geOfferInterfaces = client.getGrandExchangeOffers();
        List<GrandExchangeOfferClass> geOffers = new ArrayList<GrandExchangeOfferClass>();
        for (GrandExchangeOffer offerInterface : geOfferInterfaces) {
            geOffers.add(new GrandExchangeOfferClass(offerInterface));
        }

        Gson gson = new Gson();
        JsonObject container = new JsonObject();
        container.addProperty("type", "ge");
        JsonElement data = gson.toJsonTree(geOffers);
        container.add("data", data);
        String json = gson.toJson(container);
        // System.out.println(json);

        try {
            Files.write(Paths.get("/tmp/runelite.fifo"), (json + System.lineSeparator()).getBytes());
        } catch (IOException e) {
            System.out.println("Couldn't write to fifo!");
        }
    }
}
