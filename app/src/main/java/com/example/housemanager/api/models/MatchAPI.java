package com.example.housemanager.api.models;

/**
 * Partido de LaLiga (football-data.org v4)
 */
public class MatchAPI {
    private long id;
    private String utcDate; // mantener como ISO 8601 (p.ej. 2025-08-01T19:00:00Z)
    private String status;
    private Integer matchday; // para filtros por jornada y fallback

    private CompetitionAPI competition;
    private SeasonAPI season;

    private TeamRef homeTeam;
    private TeamRef awayTeam;

    private ScoreAPI score;

    public MatchAPI() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUtcDate() { return utcDate; }
    public void setUtcDate(String utcDate) { this.utcDate = utcDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMatchday() { return matchday; }
    public void setMatchday(Integer matchday) { this.matchday = matchday; }

    public CompetitionAPI getCompetition() { return competition; }
    public void setCompetition(CompetitionAPI competition) { this.competition = competition; }

    public SeasonAPI getSeason() { return season; }
    public void setSeason(SeasonAPI season) { this.season = season; }

    public TeamRef getHomeTeam() { return homeTeam; }
    public void setHomeTeam(TeamRef homeTeam) { this.homeTeam = homeTeam; }

    public TeamRef getAwayTeam() { return awayTeam; }
    public void setAwayTeam(TeamRef awayTeam) { this.awayTeam = awayTeam; }

    public ScoreAPI getScore() { return score; }
    public void setScore(ScoreAPI score) { this.score = score; }
}
