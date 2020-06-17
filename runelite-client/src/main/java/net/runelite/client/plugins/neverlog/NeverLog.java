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
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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
//      timer++;
//      if (timer > 2) {
//          // Executors.newSingleThreadExecutor().submit(this::getInventory);
//          // Executors.newSingleThreadExecutor().submit(this::getGEOffers);
//
////          this.getInventory();
////          this.getBank();
////          this.getGEOffers();
//          this.playerlocation();
//          timer = 0;
//      }



//        this.getPlayerLocation();

//        Executors.newSingleThreadExecutor().submit(this::getInventory);
//        Executors.newSingleThreadExecutor().submit(this::getGEOffers);
       Executors.newSingleThreadExecutor().submit(this::getPlayerLocation);

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

    private void printFifo(String type, Object obj) {
        Gson gson = new Gson();
        JsonObject container = new JsonObject();
        container.addProperty("type", type);
        JsonElement data = gson.toJsonTree(obj);
        container.add("data", data);
        String json = gson.toJson(container);
        try {
            Files.write(Paths.get("/tmp/runelite.fifo"), (json + System.lineSeparator()).getBytes());
        } catch (IOException e) {
            System.out.println("Couldn't write to fifo!");
        }
    }
    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();
        int s = state.getState();
        GameState a = GameState.LOADING;
        if (state == GameState.LOGIN_SCREEN) {
            printFifo("gamestate", "LOGIN_SCREEN");
        }
    }

    class LocationDetailed {
        public int x;
        public int y;
        public LocationDetailed() {
            this.x = -1;
            this.y = -1;
        }
        public LocationDetailed(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }

    private void getPlayerLocation() {
        final WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
        if (playerPos == null)
        {
//            return null;
            return;
        }


        final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
        if (playerPosLocal == null)
        {
//            return null;
            return;
        }

        System.out.printf("World (X: %d \t Y: %d)\n", playerPos.getX(), playerPos.getY());
        LocationDetailed location = new LocationDetailed(playerPosLocal.getX(), playerPosLocal.getY());
        this.printFifo("playerlocation", location);
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
            this.id = item.getId();
            this.quantity = item.getQuantity();
            // this.slot = slot;
            this.state = "OCCUPIED";
        }
    }

    private void getInventory() {
        final int INVENTORY_SIZE = 28;
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null)
    		{
          return;
        }
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

    private void getBank() {
      // ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
      final ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
      if (itemContainer == null)
      {
        return;
      }
      List<Item> items = new ArrayList<Item>();
  		for (Item item : itemContainer.getItems()) {
        if (item == null){
          continue;
        }
        items.add(item);
      }
      Gson gson = new Gson();
      JsonObject container = new JsonObject();
      container.addProperty("type", "bank");
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
