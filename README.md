# WeatherScreen

Простое погодное приложение на Kotlin + Jetpack Compose для Москвы.  
Один экран: текущая погода + почасовой прогноз на сегодня + прогноз на 3 дня.

## Стек

- Kotlin 2.2
- Jetpack Compose (полностью программный UI, без XML)
- Clean Architecture (data → domain → presentation)
- Hilt (DI)
- Retrofit + Moshi
- Kotlin Coroutines + Flow
- Coil Compose (загрузка иконок погоды)
- Compose Shimmer (красивый скелетон при загрузке)
- Accompanist SwipeRefresh

## Как запустить

1. Склонируй репозиторий  
   ```bash
   git clone https://github.com/DenisGubernatorov/weatherScreen.git
