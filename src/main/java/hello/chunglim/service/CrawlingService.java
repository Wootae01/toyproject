package hello.chunglim.service;

import hello.chunglim.domain.Meal;
import hello.chunglim.util.MealParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlingService {

    private final String URL = "https://dorm.chungbuk.ac.kr/home/sub.php";
    private final String MENUKEY = "menukey=20041";
    private final String TYPE = "type=";
    private final String[] NOMENU = {"등록된 식단이 없습니다."};
    private final String CUR_DAY = "cur_day=";
    private final MealParser mealParser;
    
    
    public String getUrl(int type, LocalDate date) {
        String format = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return URL + "?" + MENUKEY + "&" + CUR_DAY + format + "&" + TYPE + type;
    }


    public Meal getMeal(String url, LocalDate date, int type) {
        String format = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dayOfWeek = getDayOfWeek(date);

        try{
            Document document = Jsoup.connect(url).get();

            Element today = document.select("#" + format).first();

            String[] morning = getMenu(today, ".morning");
            String[] lunch = getMenu(today, ".lunch");
            String[] evening = getMenu(today, ".evening");

            return new Meal(morning, lunch, evening, date, dayOfWeek, type);
        }catch (IOException e) {
            return new Meal(NOMENU, NOMENU, NOMENU, date, dayOfWeek, type);
        } catch (NullPointerException e) {
            log.error("today is null={}", e);
            return new Meal(NOMENU, NOMENU, NOMENU, date, dayOfWeek, type);
        }
    }

    private String[] getMenu(Element today, String cssQuery) {
        String mealInfo =  today.select(cssQuery).first().text();
        log.info("mealInfo={}", mealInfo);
        return mealParser.parse(mealInfo);
    }

    private static String getDayOfWeek(LocalDate date) {
        String[] days = {"", "월", "화", "수", "목", "금", "토", "일"};
        return days[date.getDayOfWeek().getValue()];
    }
}
