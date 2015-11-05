import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class Tests {

  private static String[] pokemons = { "bulbasaur", "ivysaur", "venusaur", "charmander", "charmeleon", "charizard",
      "squirtle", "wartortle", "blastoise", "caterpie", "metapod", "butterfree", "weedle", "kakuna", "beedrill",
      "pidgey", "pidgeotto", "pidgeot", "rattata", "raticate", "spearow", "fearow", "ekans", "arbok", "pikachu",
      "raichu", "sandshrew", "sandslash", "nidorina", "nidoqueen", "nidoran", "nidorino", "nidoking", "clefairy",
      "clefable", "vulpix", "ninetales", "jigglypuff", "wigglytuff", "zubat", "golbat", "oddish", "gloom", "vileplume",
      "paras", "parasect", "venonat", "venomoth", "diglett", "dugtrio", "meowth", "persian", "psyduck", "golduck",
      "mankey", "primeape", "growlithe", "arcanine", "poliwag", "poliwhirl", "poliwrath", "abra", "kadabra", "alakazam",
      "machop", "machoke", "machamp", "bellsprout", "weepinbell", "victreebel", "tentacool", "tentacruel", "geodude",
      "graveler", "golem", "ponyta", "rapidash", "slowpoke", "slowbro", "magnemite", "magneton", "farfetch'd", "doduo",
      "dodrio", "seel", "dewgong", "grimer", "muk", "shellder", "cloyster", "gastly", "haunter", "gengar", "onix",
      "drowzee", "hypno", "krabby", "kingler", "voltorb", "electrode", "exeggcute", "exeggutor", "cubone", "marowak",
      "hitmonlee", "hitmonchan", "lickitung", "koffing", "weezing", "rhyhorn", "rhydon", "chansey", "tangela",
      "kangaskhan", "horsea", "seadra", "goldeen", "seaking", "staryu", "starmie", "mr. mime", "scyther", "jynx",
      "electabuzz", "magmar", "pinsir", "tauros", "magikarp", "gyarados", "lapras", "ditto", "eevee", "vaporeon",
      "jolteon", "flareon", "porygon", "omanyte", "omastar", "kabuto", "kabutops", "aerodactyl", "snorlax", "articuno",
      "zapdos", "moltres", "dratini", "dragonair", "dragonite", "mewtwo", "mew", "chikorita", "bayleef", "meganium",
      "cyndaquil", "quilava", "typhlosion", "totodile", "croconaw", "feraligatr", "sentret", "furret", "hoothoot",
      "noctowl", "ledyba", "ledian", "spinarak", "ariados", "crobat", "chinchou", "lanturn", "pichu", "cleffa",
      "igglybuff", "togepi", "togetic", "natu", "xatu", "mareep", "flaaffy", "ampharos", "bellossom", "marill",
      "azumarill", "sudowoodo", "politoed", "hoppip", "skiploom", "jumpluff", "aipom", "sunkern", "sunflora", "yanma",
      "wooper", "quagsire", "espeon", "umbreon", "murkrow", "slowking", "misdreavus", "unown", "wobbuffet", "girafarig",
      "pineco", "forretress", "dunsparce", "gligar", "steelix", "snubbull", "granbull", "qwilfish", "scizor", "shuckle",
      "heracross", "sneasel", "teddiursa", "ursaring", "slugma", "magcargo", "swinub", "piloswine", "corsola",
      "remoraid", "octillery", "delibird", "mantine", "skarmory", "houndour", "houndoom", "kingdra", "phanpy",
      "donphan", "porygon2", "stantler", "smeargle", "tyrogue", "hitmontop", "smoochum", "elekid", "magby", "miltank",
      "blissey", "raikou", "entei", "suicune", "larvitar", "pupitar", "tyranitar", "lugia", "ho-oh", "celebi",
      "treecko", "grovyle", "sceptile", "torchic", "combusken", "blaziken", "mudkip", "marshtomp", "swampert",
      "poochyena", "mightyena", "zigzagoon", "linoone", "wurmple", "silcoon", "beautifly", "cascoon", "dustox", "lotad",
      "lombre", "ludicolo", "seedot", "nuzleaf", "shiftry", "taillow", "swellow", "wingull", "pelipper", "ralts",
      "kirlia", "gardevoir", "surskit", "masquerain", "shroomish", "breloom", "slakoth", "vigoroth", "slaking",
      "nincada", "ninjask", "shedinja", "whismur", "loudred", "exploud", "makuhita", "hariyama", "azurill", "nosepass",
      "skitty", "delcatty", "sableye", "mawile", "aron", "lairon", "aggron", "meditite", "medicham", "electrike",
      "manectric", "plusle", "minun", "volbeat", "illumise", "roselia", "gulpin", "swalot", "carvanha", "sharpedo",
      "wailmer", "wailord", "numel", "camerupt", "torkoal", "spoink", "grumpig", "spinda", "trapinch", "vibrava",
      "flygon", "cacnea", "cacturne", "swablu", "altaria", "zangoose", "seviper", "lunatone", "solrock", "barboach",
      "whiscash", "corphish", "crawdaunt", "baltoy", "claydol", "lileep", "cradily", "anorith", "armaldo", "feebas",
      "milotic", "castform", "kecleon", "shuppet", "banette", "duskull", "dusclops", "tropius", "chimecho", "absol",
      "wynaut", "snorunt", "glalie", "spheal", "sealeo", "walrein", "clamperl", "huntail", "gorebyss", "relicanth",
      "luvdisc", "bagon", "shelgon", "salamence", "beldum", "metang", "metagross", "regirock", "regice", "registeel",
      "latias", "latios", "kyogre", "groudon", "rayquaza", "jirachi", "deoxys", "turtwig", "grotle", "torterra",
      "chimchar", "monferno", "infernape", "piplup", "prinplup", "empoleon", "starly", "staravia", "staraptor",
      "bidoof", "bibarel", "kricketot", "kricketune", "shinx", "luxio", "luxray", "budew", "roserade", "cranidos",
      "rampardos", "shieldon", "bastiodon", "burmy", "wormadam", "mothim", "combee", "vespiquen", "pachirisu", "buizel",
      "floatzel", "cherubi", "cherrim", "shellos", "gastrodon", "ambipom", "drifloon", "drifblim", "buneary", "lopunny",
      "mismagius", "honchkrow", "glameow", "purugly", "chingling", "stunky", "skuntank", "bronzor", "bronzong",
      "bonsly", "mime jr.", "happiny", "chatot", "spiritomb", "gible", "gabite", "garchomp", "munchlax", "riolu",
      "lucario", "hippopotas", "hippowdon", "skorupi", "drapion", "croagunk", "toxicroak", "carnivine", "finneon",
      "lumineon", "mantyke", "snover", "abomasnow", "weavile", "magnezone", "lickilicky", "rhyperior", "tangrowth",
      "electivire", "magmortar", "togekiss", "yanmega", "leafeon", "glaceon", "gliscor", "mamoswine", "porygon-z",
      "gallade", "probopass", "dusknoir", "froslass", "rotom", "uxie", "mesprit", "azelf", "dialga", "palkia",
      "heatran", "regigigas", "giratina", "cresselia", "phione", "manaphy", "darkrai", "shaymin", "arceus", "victini",
      "snivy", "servine", "serperior", "tepig", "pignite", "emboar", "oshawott", "dewott", "samurott", "patrat",
      "watchog", "lillipup", "herdier", "stoutland", "purrloin", "liepard", "pansage", "simisage", "pansear",
      "simisear", "panpour", "simipour", "munna", "musharna", "pidove", "tranquill", "unfezant", "blitzle", "zebstrika",
      "roggenrola", "boldore", "gigalith", "woobat", "swoobat", "drilbur", "excadrill", "audino", "timburr", "gurdurr",
      "conkeldurr", "tympole", "palpitoad", "seismitoad", "throh", "sawk", "sewaddle", "swadloon", "leavanny",
      "venipede", "whirlipede", "scolipede", "cottonee", "whimsicott", "petilil", "lilligant", "basculin", "sandile",
      "krokorok", "krookodile", "darumaka", "darmanitan", "maractus", "dwebble", "crustle", "scraggy", "scrafty",
      "sigilyph", "yamask", "cofagrigus", "tirtouga", "carracosta", "archen", "archeops", "trubbish", "garbodor",
      "zorua", "zoroark", "minccino", "cinccino", "gothita", "gothorita", "gothitelle", "solosis", "duosion",
      "reuniclus", "ducklett", "swanna", "vanillite", "vanillish", "vanilluxe", "deerling", "sawsbuck", "emolga",
      "karrablast", "escavalier", "foongus", "amoonguss", "frillish", "jellicent", "alomomola", "joltik", "galvantula",
      "ferroseed", "ferrothorn", "klink", "klang", "klinklang", "tynamo", "eelektrik", "eelektross", "elgyem",
      "beheeyem", "litwick", "lampent", "chandelure", "axew", "fraxure", "haxorus", "cubchoo", "beartic", "cryogonal",
      "shelmet", "accelgor", "stunfisk", "mienfoo", "mienshao", "druddigon", "golett", "golurk", "pawniard", "bisharp",
      "bouffalant", "rufflet", "braviary", "vullaby", "mandibuzz", "heatmor", "durant", "deino", "zweilous",
      "hydreigon", "larvesta", "volcarona", "cobalion", "terrakion", "virizion", "tornadus", "thundurus", "reshiram",
      "zekrom", "landorus", "kyurem", "keldeo", "meloetta", "genesect", "chespin", "quilladin", "chesnaught",
      "fennekin", "braixen", "delphox", "froakie", "frogadier", "greninja", "bunnelby", "diggersby", "fletchling",
      "fletchinder", "talonflame", "scatterbug", "spewpa", "vivillon", "litleo", "pyroar", "flabb", "floette",
      "florges", "skiddo", "gogoat", "pancham", "pangoro", "furfrou", "espurr", "meowstic", "honedge", "doublade",
      "aegislash", "spritzee", "aromatisse", "swirlix", "slurpuff", "inkay", "malamar", "binacle", "barbaracle",
      "skrelp", "dragalge", "clauncher", "clawitzer", "helioptile", "heliolisk", "tyrunt", "tyrantrum", "amaura",
      "aurorus", "sylveon", "hawlucha", "dedenne", "carbink", "goomy", "sliggoo", "goodra", "klefki", "phantump",
      "trevenant", "pumpkaboo", "gourgeist", "bergmite", "avalugg", "noibat", "noivern", "xerneas", "yveltal",
      "zygarde", "diancie", "hoopa" };

  public static ScoreSheet runTests(ScoreSheet scoreSheet) throws IOException {
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.prop");

    properties.load(input);

    scoreSheet.className = properties.getProperty("className");
    scoreSheet.homeworkName = properties.getProperty("homeworkName");
    scoreSheet.studentMax = Integer.parseInt(properties.getProperty("studentMax"));
    int timeLimitSeconds = Integer.parseInt(properties.getProperty("timeLimitSeconds"));

    Callable<ScoreSheet> tests = new Callable<ScoreSheet>() {
      @Override
      public ScoreSheet call() throws Exception {
        return tests(scoreSheet);
      }
    };

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<ScoreSheet> future = executor.submit(tests);
    executor.shutdown();

    try {
      future.get(timeLimitSeconds, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    } catch (ExecutionException e) {
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    } catch (TimeoutException e) {
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    }

    if (!executor.isTerminated()) {
      executor.shutdownNow();
    }

    postJson(scoreSheet);

    return scoreSheet;
  }

  private static void postJson(ScoreSheet scoreSheet) throws IOException {
    System.out.println(scoreSheet.toJSONString());

    String postUrl = "http://jarvis.xyz/webhook/curl";
    HttpPost post = new HttpPost(postUrl);
    StringEntity postingString = new StringEntity(scoreSheet.toJSONString());
    post.setEntity(postingString);
    post.setHeader("Content-type", "application/json");
    HttpClient httpClient = HttpClientBuilder.create().build();
    httpClient.execute(post);
  }

  private static String stackTraceToString(Exception e) {
    StringBuilder sb = new StringBuilder();
    sb.append(e.getClass().getName() + "\n");

    int stackTraceCount = 0;
    for (StackTraceElement element : e.getStackTrace()) {
      sb.append(element.toString());
      sb.append("\n");
      stackTraceCount++;
      if (stackTraceCount > 2) {
        sb.append("Stack trace redacted...");
        return sb.toString().trim();
      }
    }
    return sb.toString().trim();
  }

  /**
   * Graders, you should only edit this. No more.
   *
   * @param scoreSheet
   * @return
   * @throws IOException
   */
  private static ScoreSheet tests(ScoreSheet scoreSheet) throws IOException {
    searchTheory(scoreSheet);
    testAvlMap(scoreSheet);
    testSeparateChainingMap(scoreSheet);
    testBwogBot(scoreSheet);
    return scoreSheet;
  }

  private static void searchTheory(ScoreSheet scoreSheet) {
    File folder = new File("./" + scoreSheet.homeworkName.replaceAll("hw", "") + "/");
    File[] listOfFiles = folder.listFiles();

    HashSet<String> ignoredFileNames = new HashSet<>();
    ignoredFileNames.add("readme.md");
    ignoredFileNames.add("comments.txt");

    HashSet<String> acceptedFileExtensions = new HashSet<>();
    acceptedFileExtensions.add("txt");
    acceptedFileExtensions.add("pdf");
    acceptedFileExtensions.add("md");

    long maxFileSize = 600000; // 500kb

    String sectionName = "Theory: ";
    int sectionScoreMax = 24;

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        String fileName = listOfFiles[i].getName().toLowerCase();
        if (!ignoredFileNames.contains(fileName)) {
          if (acceptedFileExtensions.contains(getExtension(fileName))) {
            // found file
            System.out.println("Found theory file: " + fileName);
            System.out.println("Size: " + listOfFiles[i].length());
            if (listOfFiles[i].length() > maxFileSize) {
              scoreSheet.addSection(sectionName + fileName + " found", 0, sectionScoreMax,
                  "Your theory file is too big (>500kb). Please resubmit.");
              return;
            } else {
              scoreSheet.addSection(sectionName + fileName + " found", 24, sectionScoreMax, "");
              return;
            }
          }
        }
      }
    }
    scoreSheet.addSection(sectionName + "no theory submission detected", 0, sectionScoreMax,
        "We could not find your theory submission in the /4/ folder. Please place it in the folder (and not in /src/ or /bin/ or any other folders)");
  }

  private static String getExtension(String fileName) {
    String extension = "";

    int i = fileName.lastIndexOf('.');
    if (i > 0) {
      extension = fileName.substring(i + 1);
    }
    return extension;
  }

  private static void testAvlMap(ScoreSheet scoreSheet) {
    AvlMap<String, Integer> map = new AvlMap<String, Integer>();

    String sectionName;
    int sectionScoreMax;

    sectionName = "AvlMap.put(): added 4 items";
    sectionScoreMax = 6;
    try {
      for (int i = 0; i < 4; i++) {
        map.put(pokemons[i], i);
      }
      scoreSheet.addSection(sectionName, 6, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "AvlMap.get(): correct values for 4 added items";
    sectionScoreMax = 6;
    try {
      boolean passed = true;
      for (int i = 0; i < 4; i++) {
        if (map.get(pokemons[i]) != i) {
          passed = false;
        }
      }
      if (passed) {
        scoreSheet.addSection(sectionName, 6, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Wrong values from get()");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "AvlMap.put(): added same 4 keys with different values";
    sectionScoreMax = 8;
    try {
      for (int i = 0; i < 12; i++) {
        map.put(pokemons[i % 4], i);
      }
      scoreSheet.addSection(sectionName, 8, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "AvlMap.get(): correct values for 4 keys with overwritten values";
    sectionScoreMax = 7;
    try {
      boolean passed = true;
      for (int i = 8; i < 12; i++) {
        if (map.get(pokemons[i % 4]) != i) {
          passed = false;
        }
      }
      if (passed) {
        scoreSheet.addSection(sectionName, 8, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Wrong values from get()");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testSeparateChainingMap(ScoreSheet scoreSheet) {
    SeparateChainingMap<String, Integer> map = new SeparateChainingMap<String, Integer>();

    String sectionName;
    int sectionScoreMax;

    sectionName = "SeparateChainingMap.put(): added same 4 keys with different values";
    sectionScoreMax = 6;
    try {
      for (int i = 0; i < 12; i++) {
        map.put(pokemons[i % 4], i);
      }
      scoreSheet.addSection(sectionName, 6, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SeparateChainingMap.get(): correct values for 4 keys with overwritten values";
    sectionScoreMax = 6;
    try {
      boolean passed = true;
      for (int i = 8; i < 12; i++) {
        if (map.get(pokemons[i % 4]) != i) {
          passed = false;
        }
      }
      if (passed) {
        scoreSheet.addSection(sectionName, 6, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Wrong values from get()");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SeparateChainingMap.getSize(): correct value returned for 4 keys";
    sectionScoreMax = 3;
    try {
      if (map.getSize() == 4) {
        scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Wrong value from getSize()");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SeparateChainingMap.getTableSize(): correct value returned for 4 keys";
    sectionScoreMax = 3;
    try {
      if (map.getTableSize() == 8) {
        scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Wrong value from getTableSize()");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SeparateChainingMap.upsize(): table sized increased correctly";
    sectionScoreMax = 7;
    try {
      for (int i = 0; i < pokemons.length; i++) {
        map.put(pokemons[i], i);
      }
      if (map.getTableSize() == 1024) {
        scoreSheet.addSection(sectionName, 7, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "tableSize should be 1024 after 720 puts with different keys");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SeparateChainingMap.upsize(): all gets still functions correctly after upsizing";
    sectionScoreMax = 3;
    try {
      boolean passed = true;
      for (int i = 0; i < pokemons.length; i++) {
        if (i != map.get(pokemons[i])) {
          passed = false;
        }
      }
      if (passed) {
        scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "get() does not return correct values after upsize()");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testBwogBot(ScoreSheet scoreSheet) {
    BwogBot bot = new BwogBot();

    String sectionName;
    int sectionScoreMax;

    sectionName = "BwogBot.readFile(): reads file without exceptions thrown";
    sectionScoreMax = 10;
    try {
      bot.readFile("comments.txt");
      scoreSheet.addSection(sectionName, 10, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "BwogBot.getMap(): returns a valid map";
    sectionScoreMax = 3;
    try {
      Map<String, Integer> map = bot.getMap();
      if (map instanceof AvlMap<?, ?>) {
        scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
      } else if (map instanceof SeparateChainingMap<?, ?>) {
        scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "getMap() did not return a valid Map");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "BwogBot.getCount(): returns correct counts";
    sectionScoreMax = 7;
    try {
      if (bot.getCount("hamdel") == 3 && bot.getCount("hodor") == 43732 && bot.getCount("bwog") == 455
          && bot.getCount("bacchanal") == 92) {
        scoreSheet.addSection(sectionName, 7, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "Wrong counts. This may not (and probably isn't) the fault of getCount. Are you sure your readFile() is correct?");
      }

    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "BwogBot.getNMostPopularWords(): returns correct words";
    sectionScoreMax = 10;
    try {
      List<String> mostPopular = Arrays.asList(new String[] { "hodor", "the", "to", "a", "is" });
      List<String> botMostPopular = bot.getNMostPopularWords(5);
      if (compareCollections(mostPopular, botMostPopular)) {
        scoreSheet.addSection(sectionName, 10, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "getNMostPopularWords() did not return correct top n words");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static <T> boolean compareCollections(Collection<T> l1, Collection<T> l2) {
    if (l1.size() != l2.size()) {
      return false;
    }
    T i2;
    Iterator<T> l2iter = l2.iterator();
    for (T i1 : l1) {
      i2 = l2iter.next();
      if (!(i2.equals(i1))) {
        return false;
      }
    }
    return true;
  }
}
