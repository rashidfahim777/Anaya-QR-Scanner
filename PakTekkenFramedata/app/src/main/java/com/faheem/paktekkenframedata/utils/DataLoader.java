package com.faheem.paktekkenframedata.utils;

import android.content.Context;
import android.util.Log;

import com.faheem.paktekkenframedata.models.Character;
import com.faheem.paktekkenframedata.models.Move;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataLoader {

    public interface DataLoadListener {
        void onSuccess(Map<String, Character> characters);
        void onError(String error);
    }

    public static void loadCharacters(Context context, DataLoadListener listener) {
        try {
            String jsonString = loadJSONFromAsset(context);
            if (jsonString == null) {
                listener.onError("Failed to load JSON file");
                return;
            }

            Gson gson = new Gson();

            // Parse the JSON structure
            JsonElement jsonElement = JsonParser.parseString(jsonString);

            Map<String, Character> characterMap = new HashMap<>();

            // Check if it's a single character object or array of characters
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                // Check if it's a single character with "character" and "moves" fields
                if (jsonObject.has("character") && jsonObject.has("moves")) {
                    // Single character format
                    String characterName = jsonObject.get("character").getAsString();
                    Character character = parseSingleCharacter(characterName, jsonObject, gson);
                    characterMap.put(characterName.toLowerCase(), character);
                } else {
                    // Multiple characters format where each key is character name
                    for (String key : jsonObject.keySet()) {
                        JsonObject charObject = jsonObject.getAsJsonObject(key);
                        if (charObject.has("moves")) {
                            Character character = parseSingleCharacter(key, charObject, gson);
                            characterMap.put(key.toLowerCase(), character);
                        }
                    }
                }
            } else if (jsonElement.isJsonArray()) {
                // Array of characters format
                JsonArray charactersArray = jsonElement.getAsJsonArray();
                for (JsonElement element : charactersArray) {
                    if (element.isJsonObject()) {
                        JsonObject charObject = element.getAsJsonObject();
                        if (charObject.has("character") && charObject.has("moves")) {
                            String characterName = charObject.get("character").getAsString();
                            Character character = parseSingleCharacter(characterName, charObject, gson);
                            characterMap.put(characterName.toLowerCase(), character);
                        }
                    }
                }
            }

            // If no characters found, create sample with Dragunov
            if (characterMap.isEmpty()) {
                Log.w("DataLoader", "No characters found in JSON, using sample data");
                Character dragunov = createDragunovSample();
                characterMap.put("dragunov", dragunov);
            }

            listener.onSuccess(characterMap);

        } catch (Exception e) {
            Log.e("DataLoader", "Error parsing JSON", e);
            listener.onError(e.getMessage());
        }
    }

    private static Character parseSingleCharacter(String name, JsonObject jsonObject, Gson gson) {
        Character character = new Character();
        String characterId = name.toLowerCase().replaceAll("\\s+", "_");
        character.setId(characterId);
        character.setName(formatCharacterName(name));

        // Set the image resource ID using the helper method
        character.setImageResId(Character.getImageResourceId(characterId));

        // Parse moves (existing code)
        if (jsonObject.has("moves") && !jsonObject.get("moves").isJsonNull()) {
            JsonElement movesElement = jsonObject.get("moves");

            if (movesElement.isJsonArray()) {
                JsonArray movesArray = movesElement.getAsJsonArray();
                Map<String, Move> movesMap = new HashMap<>();

                for (JsonElement moveElement : movesArray) {
                    if (moveElement.isJsonObject()) {
                        JsonObject moveObject = moveElement.getAsJsonObject();
                        Move move = parseMove(moveObject, gson);
                        if (move.getCommand() != null && !move.getCommand().isEmpty()) {
                            movesMap.put(move.getCommand(), move);
                        }
                    }
                }
                character.setMoves(movesMap);
            } else if (movesElement.isJsonObject()) {
                Type moveType = new TypeToken<Map<String, Move>>(){}.getType();
                Map<String, Move> moves = gson.fromJson(movesElement, moveType);
                character.setMoves(moves);
            }
        } else {
            character.setMoves(new HashMap<String, Move>());
        }

        return character;
    }

    private static Move parseMove(JsonObject moveObject, Gson gson) {
        Move move = new Move();

        // Parse basic fields
        if (moveObject.has("command") && !moveObject.get("command").isJsonNull()) {
            move.setCommand(moveObject.get("command").getAsString());
        }

        if (moveObject.has("hit_level") && !moveObject.get("hit_level").isJsonNull()) {
            move.setHitLevel(moveObject.get("hit_level").getAsString());
        }

        if (moveObject.has("damage") && !moveObject.get("damage").isJsonNull()) {
            move.setDamage(moveObject.get("damage").getAsString());
        }

        if (moveObject.has("startup") && !moveObject.get("startup").isJsonNull()) {
            move.setStartup(moveObject.get("startup").getAsString());
        }

        if (moveObject.has("block") && !moveObject.get("block").isJsonNull()) {
            move.setBlock(moveObject.get("block").getAsString());
        }

        if (moveObject.has("hit") && !moveObject.get("hit").isJsonNull()) {
            move.setHit(moveObject.get("hit").getAsString());
        }

        if (moveObject.has("counter_hit") && !moveObject.get("counter_hit").isJsonNull()) {
            move.setCounterHit(moveObject.get("counter_hit").getAsString());
        }

        // Parse notes array
        if (moveObject.has("notes") && !moveObject.get("notes").isJsonNull()) {
            JsonElement notesElement = moveObject.get("notes");
            if (notesElement.isJsonArray()) {
                JsonArray notesArray = notesElement.getAsJsonArray();
                List<String> notes = new ArrayList<>();
                for (JsonElement noteElement : notesArray) {
                    if (!noteElement.isJsonNull()) {
                        String note = noteElement.getAsString();
                        if (note != null && !note.isEmpty()) {
                            notes.add(note);
                        }
                    }
                }
                move.setNotes(notes);
            } else if (notesElement.isJsonPrimitive()) {
                // Single note as string
                List<String> notes = new ArrayList<>();
                String note = notesElement.getAsString();
                if (note != null && !note.isEmpty()) {
                    notes.add(note);
                }
                move.setNotes(notes);
            }
        }

        return move;
    }

    private static String formatCharacterName(String name) {
        // Capitalize first letter and handle special cases
        if (name == null || name.trim().isEmpty()) return "Unknown";

        // Clean the name
        name = name.trim();

        // Split by spaces or underscores and capitalize each word
        String[] words = name.split("[ _]");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                // Capitalize first letter, make the rest lowercase
                String firstLetter = word.substring(0, 1).toUpperCase();
                String restOfWord = word.substring(1).toLowerCase();
                formattedName.append(firstLetter).append(restOfWord).append(" ");
            }
        }

        return formattedName.toString().trim();
    }

    private static Character createDragunovSample() {
        Character dragunov = new Character();
        dragunov.setId("dragunov");
        dragunov.setName("Dragunov");
        dragunov.setImageResId(Character.getImageResourceId("dragunov")); // Set image

        // Create sample moves
        Map<String, Move> moves = new HashMap<>();

        Move move1 = new Move();
        move1.setCommand("1");
        move1.setHitLevel("h");
        move1.setDamage("5");
        move1.setStartup("i10");
        move1.setBlock("+1");
        move1.setHit("+8");
        move1.setCounterHit("");
        move1.setNotes(new ArrayList<String>());
        moves.put("1", move1);

        Move move2 = new Move();
        move2.setCommand("2");
        move2.setHitLevel("h");
        move2.setDamage("10");
        move2.setStartup("i12");
        move2.setBlock("-6");
        move2.setHit("+5");
        move2.setCounterHit("");
        move2.setNotes(new ArrayList<String>());
        moves.put("2", move2);

        Move move3 = new Move();
        move3.setCommand("df+2");
        move3.setHitLevel("m");
        move3.setDamage("16");
        move3.setStartup("i16~17");
        move3.setBlock("-9");
        move3.setHit("+32a (+22)");
        move3.setCounterHit("");
        move3.setNotes(new ArrayList<String>());
        moves.put("df+2", move3);

        dragunov.setMoves(moves);
        return dragunov;
    }

    private static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("tekken_frame_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e("DataLoader", "Error loading JSON from assets", ex);
            return null;
        }
        return json;
    }

}