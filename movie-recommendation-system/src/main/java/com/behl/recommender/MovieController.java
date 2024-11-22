package com.behl.recommender;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieRecommender movieRecommender;

    public MovieController(MovieRecommender movieRecommender) {
        this.movieRecommender = movieRecommender;
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam String query) {
        List<Movie> recommendations = movieRecommender.recommend(query);
        return ResponseEntity.ok(recommendations);
    }

}