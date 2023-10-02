package com.esq.rbac.service.uam.transactionhistory.domain;
import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "rbac", name = "uamTransactionHistory")
public class UamTransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ticketNumber", length = 100, nullable = false)
    private String ticketNumber;

    @Column(name = "responseJSON")
    private String responseJSON;

    @Convert(converter = UtcDateConverter.class)
    @Column(name = "createdOn")
    private Date createdOn;

}
