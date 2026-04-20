package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.PriceList;
import com.goldencinema.backend.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    Optional<PriceList> findByTicketType(TicketType ticketType);
}
