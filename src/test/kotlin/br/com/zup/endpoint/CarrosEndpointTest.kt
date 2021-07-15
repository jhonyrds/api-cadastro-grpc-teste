package br.com.zup.endpoint

import br.com.zup.CadastroCarroGrpcServiceGrpc
import br.com.zup.CarroRequest
import br.com.zup.model.Carro
import br.com.zup.repository.CarroRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(
    val repository: CarroRepository,
    val grpcClient: CadastroCarroGrpcServiceGrpc.CadastroCarroGrpcServiceBlockingStub
) {
    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deve cadastrar um novo carro`() {
        //cenário

        //ação
        val response = grpcClient.adicionar(
            CarroRequest.newBuilder()
                .setModelo("Gol")
                .setPlaca("ORA-2021")
                .build()
        )

        //validação
        with(response) {
            assertNotNull(id)
            assertTrue(repository.existsById(id))
        }
    }

    @Test
    internal fun `nao deve adicionar novo carro quando placa ja existente`() {

        //cenário
        val existente = repository.save(Carro("Gol", "ORA-2021"))

        //ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                    .setModelo("Ferrari")
                    .setPlaca(existente.placa)
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Placa já foi cadastrada", status.description)
        }
    }

    @Test
    internal fun `nao deve adicionar novo carro quando dados de entrada forem invalidos`() {

        //cenário

        //ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                    .setModelo("")
                    .setPlaca("")
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("dados inválidos", status.description)
            // TODO: verificar as violações da bean validation
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CadastroCarroGrpcServiceGrpc.CadastroCarroGrpcServiceBlockingStub? {
            return CadastroCarroGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}