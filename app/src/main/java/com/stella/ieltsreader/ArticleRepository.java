package com.stella.ieltsreader;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class ArticleRepository {
    private static final String PREFS = "ielts_reader";

    public static List<Article> all(Context context) {
        List<Article> result = new ArrayList<>();
        result.add(new Article("cities", "URBAN LIFE · BAND 7", "The fifteen-minute city comes of age",
                "For decades, urban planners assumed that progress meant travelling farther and faster. A different idea is now gaining momentum: residents should be able to reach work, shops, parks and essential services within a short walk or bicycle ride. Supporters argue that this compact model can reduce congestion, improve public health and make neighbourhoods more resilient.\n\nThe proposal is not without controversy. Critics fear that reorganising streets may restrict drivers or accelerate the rising cost of housing in desirable districts. Yet the most successful schemes are pragmatic rather than ideological. They add safe crossings, reliable buses and small public spaces while consulting the people who already live nearby.\n\nEvidence remains incomplete, but early results are encouraging. When daily necessities are accessible, people tend to make shorter journeys and spend more money with local businesses. The broader lesson is that a liveable city is not defined by how quickly people can escape it, but by how comfortably they can remain.", 5));
        result.add(new Article("oceans", "ENVIRONMENT · BAND 7", "Listening to the hidden life of the ocean",
                "The ocean is often described as silent, yet it contains a complex acoustic world. Whales exchange calls across vast distances, fish produce signals during courtship, and even tiny organisms contribute to a constant underwater chorus. Marine researchers are increasingly using sensitive microphones to measure these sounds without disturbing the animals that create them.\n\nThis approach has a significant advantage. Traditional surveys provide only a brief snapshot, whereas autonomous recorders can remain at sea for months. Scientists can therefore detect seasonal migration, identify unfamiliar species and assess how ecosystems respond to storms or warmer water.\n\nHuman activity also leaves an audible footprint. Shipping, construction and sonar may obscure natural signals that animals depend on. Regulators are now considering quieter engines and temporary limits in vulnerable habitats. Protecting the ocean, researchers suggest, requires us not only to observe it but also to listen carefully.", 4));
        result.add(new Article("work", "SOCIETY · BAND 8", "Why deep work is becoming a scarce skill",
                "Modern offices reward rapid communication, but constant availability can fragment attention. Each notification appears trivial; together they impose a substantial cognitive cost. After an interruption, employees may need several minutes to recover the concentration required for demanding analysis or creative thought.\n\nSome organisations have responded by establishing periods with no meetings or internal messages. The intention is not to eliminate collaboration. Instead, it is to distinguish between tasks that benefit from immediate discussion and those that require sustained individual effort. Early experiments suggest that workers often complete complex assignments faster when their calendars contain protected time.\n\nThere is no universal formula. A hospital and a design studio have different obligations, and emergencies cannot be scheduled. Nevertheless, treating attention as a finite resource encourages managers to evaluate whether every message is genuinely urgent. In an economy flooded with information, the ability to concentrate may become a durable competitive advantage.", 4));

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        try {
            JSONArray saved = new JSONArray(prefs.getString("custom_articles", "[]"));
            for (int i = 0; i < saved.length(); i++) {
                JSONObject item = saved.getJSONObject(i);
                result.add(0, new Article(item.getString("id"), "YOUR ARTICLE · PRACTICE",
                        item.getString("title"), item.getString("body"),
                        Math.max(1, item.getString("body").split("\\s+").length / 180)));
            }
        } catch (Exception ignored) { }
        return result;
    }

    public static Article find(Context context, String id) {
        for (Article article : all(context)) if (article.id.equals(id)) return article;
        return all(context).get(0);
    }

    public static void saveCustom(Context context, String title, String body) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        JSONArray saved = new JSONArray(prefs.getString("custom_articles", "[]"));
        JSONObject item = new JSONObject();
        item.put("id", "custom_" + System.currentTimeMillis());
        item.put("title", title);
        item.put("body", body);
        saved.put(item);
        prefs.edit().putString("custom_articles", saved.toString()).apply();
    }

    private ArticleRepository() { }
}
