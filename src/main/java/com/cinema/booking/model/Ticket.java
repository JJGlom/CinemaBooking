package com.cinema.booking.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketIdentifier;

    @Enumerated(EnumType.STRING)
    private TicketType type;

    private BigDecimal price;

    private LocalDateTime purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Screening screening;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Seat seat;

    private boolean paid;

    @PrePersist
    public void prePersist() {
        if (ticketIdentifier == null) {
            ticketIdentifier = UUID.randomUUID().toString(); // Automatyczne generowanie UUID
        }
        if (purchaseDate == null) {
            purchaseDate = LocalDateTime.now();
        }
    }
}