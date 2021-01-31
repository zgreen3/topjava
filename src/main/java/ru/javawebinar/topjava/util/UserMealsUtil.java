package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMeal> mealsFiltered = new ArrayList<>();
        Map<LocalDate, Integer> dateCaloriesSumMap = new HashMap<>();
        for (UserMeal meal : meals) {
            LocalDateTime dateTime = meal.getDateTime();
            int dateCalSumBuf = dateCaloriesSumMap.getOrDefault(dateTime.toLocalDate(), 0);
            dateCaloriesSumMap.put(dateTime.toLocalDate(), meal.getCalories() + dateCalSumBuf);

            if (TimeUtil.isBetweenHalfOpen(dateTime.toLocalTime(), startTime, endTime)) {
                mealsFiltered.add(meal);
            }
        }
        List<UserMealWithExcess> mealsWithExcess = new ArrayList<>();
        for (UserMeal mealFiltered : mealsFiltered) {
            mealsWithExcess.add(createUserMealWithExcess(mealFiltered,
                    caloriesPerDay < dateCaloriesSumMap.getOrDefault(mealFiltered.getDateTime().toLocalDate(), 0)));
        }
        return mealsWithExcess;
    }

    /**
     * Optional HW0 realization using one stream
     *
     * @param meals          an object of UserMeal class, not null
     * @param startTime      not null
     * @param endTime        not null
     * @param caloriesPerDay not null
     * @return a list of UserMealWithExcess objects that are in between startTime and endTime half open interval [startTime, endTime)
     * for all days in meals list with a boolean excess field as a result of comparison with caloriesPerDay
     */
    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        return
                meals.stream().collect(groupingBy(meal -> meal.getDateTime().toLocalDate())).values().stream().map(
                        userMeals -> {
                            int dateCalSumBuf = userMeals.stream().mapToInt(UserMeal::getCalories).sum();
                            return userMeals.stream().map(userMeal -> {
                                        if (TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                                            return createUserMealWithExcess(userMeal, caloriesPerDay < dateCalSumBuf);
                                        } else {
                                            return null;
                                        }
                                    }
                            ).filter(Objects::nonNull).collect(Collectors.toList());
                        }
                ).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Helper method as alternative to constructor in UserMealWithExcess to make UserMealWithExcess independent from UserMeal and UserMealsUtil
     *
     * @param userMeal an object of class UserMeal that is taken as a base to construct UserMealWithExcess object, not null
     * @param excess   a boolean value of a whole day excess in calories as a result of comparison with caloriesPerDay, not null
     * @return a new UserMealWithExcess object with excess supplied as a method parameter
     */
    public static UserMealWithExcess createUserMealWithExcess(UserMeal userMeal, boolean excess) {
        return new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(), excess);
    }
}
