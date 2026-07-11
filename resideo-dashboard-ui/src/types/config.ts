export interface ConfigFileInfo {
  path: string;
  fileName: string;
  extension: string;
  size: number;
  keyCount: number;
}

export interface FieldValue {
  name: string;
  label: string;
  type: string;
  value?: string;
  defaultValue?: string;
  placeholder?: string;
  options?: string[];
  required?: boolean;
  min?: number;
  max?: number;
  map?: string;
  mapIfTrue?: string;
  source?: string;
}

export interface ParameterGroup {
  group: string;
  fields: FieldValue[];
}

export interface MergedConfig {
  groups: ParameterGroup[];
  additionalConfig: Record<string, string>;
  commandTemplate: string;
  selectedFilePath: string;
}

export interface DashboardConfig {
  frameworkName?: string;
  buildTool?: string;
  commandTemplate?: string;
  parameters?: {
    group: string;
    fields: {
      name: string;
      label: string;
      type: string;
      configKey?: string;
      defaultValue?: string;
      placeholder?: string;
      options?: string[];
      required?: boolean;
      map?: string;
      mapIfTrue?: string;
    }[];
  }[];
}
