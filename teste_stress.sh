#!/bin/bash

# 1. Criar Pauta
echo "--------------------------------------"
echo "Etapa 1: Criando pauta..."
PAUTA_RESPONSE=$(curl -s -X POST http://localhost:8080/v1/pautas \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Teste com Seed de 100 Votos", "descricao": "Validando carga inicial"}')

PAUTA_ID=$(echo $PAUTA_RESPONSE | sed 's/.*"id":\([0-9]*\).*/\1/')
echo "Pauta criada com ID: $PAUTA_ID"

# 2. Abrir Sessão
echo "--------------------------------------"
echo "Etapa 2: Abrindo sessão para Pauta $PAUTA_ID..."
curl -X POST "http://localhost:8080/v1/pautas/$PAUTA_ID/sessao?minutos=1"

sleep 1

# 3. Validar se a sessão subiu (Aqui você já deve ver os 100 votos se a pauta for a mesma do seed)
echo -e "\n--------------------------------------"
echo "Etapa 3: Verificando sessões no banco..."
curl -s -X GET http://localhost:8080/v1/pautas/sessoes

# 4. Loop de Votos Adicionais (Stress manual)
echo -e "\n--------------------------------------"
echo "Etapa 4: Adicionando +50 votos via script..."
for i in {1..50}
do
   CPF=$(printf "%011d" $i)
   curl -s -X POST "http://localhost:8080/v1/pautas/$PAUTA_ID/votos" \
     -H "Content-Type: application/json" \
     -d "{\"associadoId\": \"$CPF\", \"escolha\": \"SIM\"}" &
done
wait

# 5. Verificar Idempotência
echo -e "\n--------------------------------------"
echo "Etapa 5: Verificar Idempotência"
echo "Tentando votar DE NOVO com o CPF 00000000001 (Deve falhar)..."
# Se o seu sistema tiver a regra de 1 voto por CPF, este comando deve dar erro.
curl -i -X POST "http://localhost:8080/v1/pautas/$PAUTA_ID/votos" \
  -H "Content-Type: application/json" \
  -d "{\"associadoId\": \"00000000001\", \"escolha\": \"NAO\"}"

echo -e "\n--------------------------------------"
echo "Fim dos testes. Se o seu seed funcionou, o resultado final deve ser 150!"