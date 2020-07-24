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
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.inject.Inject;

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

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static net.runelite.api.Perspective.localToCanvas;

import io.javalin.Javalin;

@PluginDescriptor(
        name = "Never Log",
        description = "Enable this and you will never log out"
)
@SuppressWarnings("unused")

public class NeverLog extends Plugin {
    @Inject
    private Client client;

    public void runJavalin() {
        Javalin app = Javalin.create().start(8080);
        app.routes(() -> {
            get("liveness", ctx -> ctx.result("Ready"));
            get("state", ctx -> ctx.result(this.getGameState()));
            get("location", ctx -> ctx.result(this.getPlayerLocation()));
            get("inventory", ctx -> ctx.result(this.getInventory()));
            get("bank", ctx -> ctx.result(this.getBank()));
            get("ge", ctx -> ctx.result(this.getGEOffers()));
            path("h", () -> {
                get("liveness", ctx -> ctx.result("Ready" + '\n'));
                get("state", ctx -> ctx.result(this.getGameState() + '\n'));
                get("location", ctx -> ctx.result(this.getPlayerLocation() + '\n'));
                get("inventory", ctx -> ctx.result(this.getInventory() + '\n'));
                get("bank", ctx -> ctx.result(this.getBank() + '\n'));
                get("ge", ctx -> ctx.result(this.getGEOffers() + '\n'));
            });
        });

    }

    // Called upon plugin startup?
    // Look into putting GRPC stuff --HERE--
    @Override
    protected void startUp() {
        Executors.newSingleThreadExecutor().submit(this::runJavalin);
    }

    // Called upon plugin removal?
    @Override
    protected void shutDown() {

    }

    @Subscribe
    public void onGameTick(GameTick event) {

    }

    private String toJson(String type, Object obj) {
        Gson gson = new Gson();
        JsonObject container = new JsonObject();
        container.addProperty("type", type);
        JsonElement data = gson.toJsonTree(obj);
        container.add("data", data);
        String result = gson.toJson(container);
        return result.equals("") ? "[]" : result;
    }

//    @Subscribe
//    public void onGameStateChanged(GameStateChanged event) {
//        GameState state = event.getGameState();
//        int s = state.getState();
//        GameState a = GameState.LOADING;
//        if (state == GameState.LOGIN_SCREEN) {
//            printFifo("gamestate", "LOGIN_SCREEN");
//        }
//    }

    private String getGameState() {
        GameState state = client.getGameState();
        return toJson("gamestate", state.toString());
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

    private String getPlayerLocation() {
        final Player player = client.getLocalPlayer();
        final WorldPoint playerPosWorld;
        if (player != null) {
            playerPosWorld = player.getWorldLocation();
        } else {
            return "";
        }

        final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPosWorld);
        if (playerPosLocal == null) {
            return "";
        }

        // System.out.printf("World (X: %d \t Y: %d)\n", playerPosWorld.getX(), playerPosWorld.getY());
        // LocationDetailed location = new LocationDetailed(playerPosLocal.getX(), playerPosLocal.getY());
//        Tile t = client.getSelectedSceneTile();
//        Point p = null;
//        if (t != null) {
//            p = localToCanvas(client, t.getLocalLocation(), 0);
//        }
//        System.out.printf("Selected tile pixel location: (X: %d \t Y: %d)\n", p.getX(), p.getY());


        // Pixel location starts at the top left corner of the "game area", not necessarily the entire window
        // Need to add the thickness of the title bar to get accurate screen locations

        LocationDetailed location = new LocationDetailed(playerPosWorld.getX(), playerPosWorld.getY());

        return this.toJson("playerlocation", location);
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
            this.state = state;
        }

        public ItemDetailed(Item item) {
            this.id = item.getId();
            this.quantity = item.getQuantity();
            this.state = "OCCUPIED";
        }
    }

    private String getInventory() {
        final int INVENTORY_SIZE = 28;
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return "[]";
        }
        List<ItemDetailed> items = new ArrayList<>();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            items.add(new ItemDetailed());
        }

        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            Item item = itemContainer.getItem(slot);
            if (item != null) {
                if (item.getQuantity() > 0) {
                    items.set(slot, new ItemDetailed(item));
                } else {
                    items.set(slot, new ItemDetailed("ERROR"));
                }
            }
        }
        return this.toJson("inventory", items);
    }

    private String getBank() {
        // ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
        if (itemContainer == null) {
            return "[]";
        }
        List<Item> items = new ArrayList<>();
        for (Item item : itemContainer.getItems()) {
            if (item != null) {
                items.add(item);
            }
        }
        return this.toJson("bank", items);
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

    private String getGEOffers() {
        GrandExchangeOffer[] geOfferInterfaces = client.getGrandExchangeOffers();
        List<GrandExchangeOfferClass> geOffers = new ArrayList<>();
        for (GrandExchangeOffer offerInterface : geOfferInterfaces) {
            if (offerInterface != null) {
                geOffers.add(new GrandExchangeOfferClass(offerInterface));
            }
        }
        geOffers = geOffers.stream().filter(Objects::isNull).collect(Collectors.toList());

        if (geOffers.size() == 0) {
            return "[]";
        }

        return this.toJson("ge", geOffers);
    }
}
