export interface Device {
  id: string;
  name: string;
  platform: string;
  udid: string;
  osVersion: string;
  cloudProvider: string;
  status: string;
  reservedBy: string;
  reservedUntil: string;
  lastHeartbeat: string;
  createdAt: string;
}

export interface Thermostat {
  id: string;
  name: string;
  serialPort: string;
  firmwareVersion: string;
  status: string;
  reservedBy: string;
  currentExecutionId: string;
  lastHeartbeat: string;
  createdAt: string;
}
