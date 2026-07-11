export class ExecutionWebSocket {
  private ws: WebSocket | null = null;
  private handlers = new Map<string, (data: unknown) => void>();

  connect() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const url = `${protocol}//${window.location.host}/ws/executions`;
    this.ws = new WebSocket(url);

    this.ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        const handler = this.handlers.get(msg.type);
        if (handler) handler(msg);
      } catch { /* ignore */ }
    };

    this.ws.onclose = () => {
      setTimeout(() => this.connect(), 3000);
    };
  }

  subscribe(executionId: string) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(`subscribe:${executionId}`);
    }
  }

  on(event: string, handler: (data: unknown) => void) {
    this.handlers.set(event, handler);
  }

  disconnect() {
    this.ws?.close();
    this.ws = null;
  }
}

export const executionWs = new ExecutionWebSocket();
