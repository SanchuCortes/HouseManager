# HouseManager (Android · Java)
App tipo “fantasy” de LaLiga para crear ligas privadas, gestionar plantillas, fichar jugadores en un **mercado diario por liga** y puntuar según el rendimiento real. Proyecto de fin de ciclo (DAM).

> Decisiones clave:
> - Arquitectura **MVC** simple + repositorios.
> - Datos reales (sin mocks en producción).
> - **Room** con migraciones y transacciones para coherencia por **liga**.
> - **Mercado diario por liga** (10 jugadores/día, sin reposición).
> - **Capitán por liga** (x2 por partido).
> - Home con **próximos partidos** en zona **Europe/Madrid**.

------------------------------------------------------------

## 1) Qué hace
- Crear/entrar en **ligas** con presupuesto inicial configurable.
- Generar **plantilla inicial** (22–24 jugadores, composición equilibrada).
- **Mercado diario** (10 jugadores por liga), compra con descuento de presupuesto.
- **Mi equipo**: capitán, precio, puntos acumulados por jugador.
- **Clasificación** por liga (suma de puntos de plantilla).
- **Puntos** por partido según reglas (ver §5).
- **Home**: próximos partidos de la jornada actual (o próximos 7 días si no hay matchday), con marcador si han terminado.

## 2) Stack técnico
- Lenguaje: **Java** (Android).
- Arquitectura: **MVC** + repositorios (controlador) + mappers.
- Persistencia: **Room** (DAOs, Entities, Migrations).
- Red: **Retrofit** + **OkHttp** (interceptor de logs), **Gson**.
- Imágenes: **Glide**.
- Estado/UI: **ViewModel** + **LiveData**.
- Config: `BuildConfig` (baseUrl, apiKey) a partir de `gradle.properties`.

## 3) Estructura de carpetas (resumen)
app/src/main/java/com/example/housemanager/
├─ api/                 # Retrofit, servicios remotos, ApiClient
├─ model/               # Modelos API, dominio y mappers
│  ├─ db/dao/           # DAOs Room
│  └─ db/entities/      # Entities Room
├─ repository/          # Repositorios (controlador/UseCases)
├─ ui/
│  ├─ main/             # MainActivity
│  ├─ auth/             # Login
│  ├─ leagues/          # Crear/Unirse/Detalle de liga
│  ├─ team/             # Mi equipo (+ adapters)
│  ├─ market/           # Mercado diario por liga
│  └─ standings/        # Clasificación
└─ viewmodel/           # ViewModels por feature

Nota: evito clases “globales” en la raíz. Todo lo de ligas vive en `ui/leagues` & `viewmodel/leagues`, etc.

## 4) Datos y sincronización
Fuente: `football-data.org v4` (equipos, plantillas, calendario y eventos disponibles).
- Primer arranque: descarga equipos + plantillas de LaLiga y los persiste en Room.
- Refresco diario: una vez al día, refresco de equipos/jugadores.
- Home: al abrir, sync breve de próximos partidos.
- Si falla la API: no inventa datos. La UI muestra estados vacíos.

## 5) Reglas de puntuación (por partido)
- Participación
  - Titular: **+2**
  - Suplente con **minutos**: **+1** (se infiere por eventos/sustitución).
- Goles (según posición)
  - Portero/Defensa: **+6**
  - Medio: **+5**
  - Delantero: **+4**
  - Gol en propia: **0**
- Asistencia: **+3**
- Tarjetas
  - Amarilla: **−1** (acumulable)
  - Doble amarilla: **−3 adicional** (neto **−4**)
  - Roja directa: **−4** (sin acumular amarillas)
- Resultado del equipo (si jugó)
  - Victoria: **+3**
  - Empate: **+1**
  - Derrota: **0**
- Capitán: x2 sobre los puntos de **ese partido** en **esa liga**.

Historizo los puntos por partido en `PlayerMatchPoints` y actualizo `PlayerEntity.totalPoints` con el sumatorio. El x2 del capitán se aplica **al insertar** los puntos de esa liga/jornada (no en el agregado).

## 6) Mercado diario por liga
- 10 jugadores/día por liga, elegidos aleatoriamente entre los disponibles en Room y **excluyendo** los ya fichados **en esa liga**.
- No se reponen hasta el día siguiente.
- Countdown real hasta medianoche local (CEST/CET).
- Compra:
  - Descuenta precio del presupuesto del usuario **en esa liga**.
  - Inserta en `LeaguePlayerOwnership(leagueId, playerId, owner)` (PK compuesta).
  - Marca el listing como vendido **solo** en esa liga.

## 7) Pantallas
- Login (demo/local).
- Home: próximos partidos (día abreviado + hora **Europe/Madrid**; marcador si finalizado).
- Crear/Unirse a liga (presupuesto inicial).
- Mi equipo: lista con capitán, precio, puntos totales.
- Mercado: 10 jugadores con equipo, posición, precio y puntos acumulados.
- Clasificación: ranking por suma de puntos de plantilla del usuario.

## 8) Persistencia y coherencia por liga
- `LeaguePlayerOwnership` y `MarketListing` usan **PK compuesta (leagueId, playerId)**.
- Fichajes y puntuación por partido se insertan en **transacción Room**.
- Migraciones activas al cambiar esquema (versión DB incrementada).

## 9) Configuración y compilación
Requisitos
- Android Studio actual (Koala+ recomendado), JDK 17, SDK 35.
- Clave de `football-data.org` (gratuita con registro).

Pasos
1. Crea/edita `gradle.properties` (en el proyecto o en tu usuario):
   FOOTBALL_API_BASE_URL=https://api.football-data.org/v4/
   FOOTBALL_API_KEY=TU_CLAVE_AQUI

2. El `build.gradle` del módulo `app` expone estos valores como `BuildConfig`:
   buildConfigField "String", "FOOTBALL_API_BASE_URL", ""${FOOTBALL_API_BASE_URL}""
   buildConfigField "String", "FOOTBALL_API_KEY", ""${FOOTBALL_API_KEY}""

3. Compila y ejecuta en debug.

Nota de seguridad: para entrega académica vale; si publicas en GitHub, no subas tu clave real.

## 10) Pruebas y validación
Unitarias (JUnit)
- `PointsCalculatorTest`: titular, suplente con/sin minutos, goles por posición, asistencia, amarillas/doble amarilla/roja, resultado y capitán x2 aplicado en inserción por liga.

Instrumentadas (Espresso)
- Home carga lista (o muestra estado vacío).
- Mercado muestra 10; comprar 1 → queda en **Mi equipo** de esa liga y el listing desaparece en esa liga.

Verificación con Database Inspector
- ¿Hay histórico?
  SELECT matchday, COUNT(*) rows, SUM(points) sum_points
  FROM PlayerMatchPoints
  GROUP BY matchday;

- ¿Mi equipo por liga?
  SELECT p.name FROM PlayerEntity p
  JOIN LeaguePlayerOwnership o ON o.playerId = p.playerId
  WHERE o.leagueId = :L
  ORDER BY p.name;

## 11) Accesibilidad/UX
- Textos legibles, estados vacíos y spinners donde carga.
- Formatos coherentes (precios, puntos).
- Errores de red con mensajes simples (sin toasts innecesarios).

## 12) Limitaciones conocidas
- Si la API no aporta alineaciones/eventos, no se puntúan esos aspectos (no invento).
- No hay multicuenta real; el “usuario” es local/demo.
- Notificaciones push fuera de alcance.

## 13) Backlog breve
- Persistir **capitán** en tabla dedicada (si aún está en prefs por liga).
- Más reglas: porterías a cero, goles encajados, penaltis fallados.
- Mejoras UI: filtros/orden en Mercado y Mi equipo.

## 14) Checklist de aceptación
- [ ] Compila y arranca sin crash.
- [ ] Crear liga con presupuesto X → plantilla inicial coherente y persistida.
- [ ] Mercado muestra **10** por liga, countdown estable, compra funciona y pasa a **Mi equipo** (misma liga).
- [ ] Puntos coherentes en Mi equipo/Mercado/Clasificación tras partidos finalizados; capitán x2 por partido/ liga.
- [ ] Home muestra jornada actual (o 7 días) con hora **Europe/Madrid** y marcador si finalizado.
- [ ] Sin mocks en producción.
- [ ] README + docs en `/docs` + pruebas mínimas.

## 15) Créditos y licencia
Proyecto académico de Santi (DAM). Librerías con licencias respectivas (Retrofit, OkHttp, Glide).
Datos de `football-data.org` con los límites de su API.
