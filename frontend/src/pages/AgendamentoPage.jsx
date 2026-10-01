import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api/client';

export default function AgendamentoPage() {
  const { id } = useParams();
  const [agendamento, setAgendamento] = useState(null);
  const [resultadoCancelamento, setResultadoCancelamento] = useState(null);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  async function carregar() {
    try {
      setAgendamento(await api.get(`/api/agendamentos/${id}`));
    } catch (e) {
      setErro(e.message);
    }
  }

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function executar(acao) {
    setErro(null);
    setCarregando(true);
    try {
      if (acao === 'pagar') {
        await api.post('/api/webhooks/pagamento', { agendamentoId: id, pago: true });
      } else if (acao === 'cancelar') {
        setResultadoCancelamento(await api.post(`/api/agendamentos/${id}/cancelar`, {}));
      } else if (acao === 'no-show') {
        await api.post(`/api/agendamentos/${id}/no-show`, {});
      }
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  if (!agendamento) {
    return <div>{erro ? <p className="erro">{erro}</p> : <p>Carregando...</p>}</div>;
  }

  return (
    <div>
      <h1>Agendamento</h1>
      <p>
        Status: <strong>{agendamento.status}</strong>
      </p>
      <p>Início: {new Date(agendamento.inicio).toLocaleString('pt-BR')}</p>
      <p>Fim: {new Date(agendamento.fim).toLocaleString('pt-BR')}</p>
      <p>Valor do serviço: R$ {agendamento.valorServico}</p>
      <p>Valor do sinal: R$ {agendamento.valorSinal}</p>

      <div className="acoes">
        <button onClick={() => executar('pagar')} disabled={carregando || agendamento.status !== 'PENDENTE_PAGAMENTO'}>
          Simular pagamento
        </button>
        <button onClick={() => executar('cancelar')} disabled={carregando}>
          Cancelar
        </button>
        <button onClick={() => executar('no-show')} disabled={carregando}>
          Marcar no-show
        </button>
      </div>

      {erro && <p className="erro">{erro}</p>}

      {resultadoCancelamento && (
        <div className="aviso-sucesso">
          <p>Valor retido: R$ {resultadoCancelamento.valorRetido}</p>
          <p>Valor reembolsado: R$ {resultadoCancelamento.valorReembolsado}</p>
        </div>
      )}
    </div>
  );
}
