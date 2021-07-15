package br.com.zup.endpoint

import br.com.zup.CadastroCarroGrpcServiceGrpc
import br.com.zup.CarroRequest
import br.com.zup.CarroResponse
import br.com.zup.model.Carro
import br.com.zup.repository.CarroRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val repository: CarroRepository):CadastroCarroGrpcServiceGrpc.CadastroCarroGrpcServiceImplBase() {
    override fun adicionar(request: CarroRequest?, responseObserver: StreamObserver<CarroResponse>?) {
        if (repository.existsByPlaca(request?.placa)){
            responseObserver?.onError(Status.ALREADY_EXISTS
                .withDescription("Placa já foi cadastrada")
                .asRuntimeException())
            return
        }

        val carro = Carro(
            modelo = request!!.modelo,
            placa = request!!.placa
        )

        try {
            repository.save(carro)
        }catch (e: ConstraintViolationException) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("dados inválidos")
                .asRuntimeException())
            return
        }

        responseObserver?.onNext(CarroResponse.newBuilder().setId(carro.id!!).build())
        responseObserver?.onCompleted()
    }
}