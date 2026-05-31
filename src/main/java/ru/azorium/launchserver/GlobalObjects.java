package ru.azorium.launchserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class GlobalObjects {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static ProgressBarStyle pbStyle = ProgressBarStyle.builder()
            .refreshPrompt("\r")
            .leftBracket("\u001b[32m")
            .delimitingSequence("\u001b[90m")
            .rightBracket( "\u001b[0m")
            .block('━')
            .space('━')
            .fractionSymbols(" ╸")
            .rightSideFractionSymbol('╺')
            .build();
}
