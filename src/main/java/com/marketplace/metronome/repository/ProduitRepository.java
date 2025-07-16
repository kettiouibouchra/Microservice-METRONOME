package com.marketplace.metronome.repository;
import com.marketplace.metronome.DAO.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProduitRepository extends JpaRepository<Produit,String>{

}
