package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}