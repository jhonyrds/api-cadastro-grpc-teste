package br.com.zup.repository

import br.com.zup.model.Carro
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
interface CarroRepository: CrudRepository<Carro, Long> {
    abstract fun existsByPlaca(placa: String?): Boolean
}