package com.bot.CareerTelegramBot.service.impl;

import com.bot.CareerTelegramBot.service.SearchCareersService;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Service
public class SearchCareersServiceImpl implements SearchCareersService {

    @Override
    public List<String> getCareersFromDjinni (String req) {
        var careersList = new ArrayList<String>();
        try {
            for (int i = 1; i <= 10; i++) {

                var doc = Jsoup.connect("https://djinni.co/jobs/?all-keywords=&any-of-keywords=&exclude-keywords=&primary_keyword="+req+"&page="+i).get();
                var careers = doc.select(".list-jobs__item");

                for (var career : careers){
                    if(!Objects.requireNonNull(career.selectFirst("div.text-date")).ownText().trim()
                            .equals("сьогодні")) break;

                    var careerBuilder = new StringBuilder();
                    careerBuilder
                            .append("\n")
                            .append(Objects.requireNonNull(career.selectFirst("div.text-date")).ownText().trim())
                            .append("\n")
                            .append(career.select(".list-jobs__title a.profile").text()).append("\n")
                            .append("djinni.co").append(career.select(".list-jobs__title a.profile").attr("href"))
                            .append("\n")
                            .append(career.select(".mt-2 .list-jobs__details .list-jobs__details__info nobr").text())
                            .append("\n");
                    careersList.add(careerBuilder.toString());
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return careersList;
    }

    @Override
    public List<String> getCareersFromDou (String req) {
        var calendar = Calendar.getInstance();
        var careersList = new ArrayList<String>();

        try {
            var doc = Jsoup.connect("https://jobs.dou.ua/vacancies/?search="+req).get();
            var careers = doc.select(".l-vacancy:not(.__hot) .vacancy");

            for (var career : careers){
                if(!Objects.requireNonNull(career.selectFirst("div.date")).ownText().trim()
                        .split(" ", 2)[0]
                        .equals(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)))) break;

                var careerBuilder = new StringBuilder();
                careerBuilder
                        .append("\n")
                        .append(new SimpleDateFormat("dd MMMM").format(calendar.getTime())).append("\n")
                        .append(career.select(".title a.vt").text()).append("\n")
                        .append(career.select(".title a.vt").attr("href")).append("\n");
                careersList.add(careerBuilder.toString());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return careersList;
    }
}
