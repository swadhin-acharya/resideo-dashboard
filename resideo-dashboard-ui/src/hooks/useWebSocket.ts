import { useEffect, useRef, useCallback } from 'react';
import { WsMessage } from '../types/execution';

type WsCallback = (msg: WsMessage) => void;

const WS_URL = import.meta.env.VITE_WS_URL ||
  `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/executions`;

export function useWebSocket(onMessage: WsCallback, executionId?: string) {
  const wsRef = useRef<WebSocket | null>(null);
  const cbRef = useRef<WsCallback>(onMessage);
  cbRef.current = onMessage;

  const connect = useCallback(() => {
    if (wsRef.current?.readyState === WebSocket.OPEN) return;

    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      if (executionId) {
        ws.send(`subscribe:${executionId}`);
      } else {
        ws.send('subscribe-dashboard');
      }
    };

    ws.onmessage = (event) => {
      try {
        const msg: WsMessage = JSON.parse(event.data);
        cbRef.current(msg);
      } catch { /* ignore */ }
    };

    ws.onclose = () => {
      if (wsRef.current === ws) {
        wsRef.current = null;
        setTimeout(connect, 3000);
      }
    };

    ws.onerror = () => ws.close();
  }, [executionId]);

  useEffect(() => {
    connect();
    return () => {
      if (wsRef.current) {
        wsRef.current.close();
        wsRef.current = null;
      }
    };
  }, [connect]);

  return wsRef;
}
