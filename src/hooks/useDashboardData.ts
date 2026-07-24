import { useEffect, useState } from 'react'
import { withBase } from '../config/AppConfigContext'
import type {
  SummaryData,
  RecentExecutionRow,
  FeatureSummary,
  FailureSummary,
  CategorySummary,
  EnvironmentInfo,
  TrendPoint,
} from '../types/models'

interface FetchState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

/**
 * Generic fetcher for the static JSON produced by the Allure processor.
 * Every screen reads pre-aggregated data only — no calculation happens here.
 */
function useJsonData<T>(path: string): FetchState<T> {
  const [state, setState] = useState<FetchState<T>>({ data: null, loading: true, error: null })

  useEffect(() => {
    let cancelled = false
    setState({ data: null, loading: true, error: null })
    fetch(withBase(path))
      .then((res) => {
        if (!res.ok) throw new Error(`Failed to load ${path} (${res.status})`)
        return res.json()
      })
      .then((data) => {
        if (!cancelled) setState({ data, loading: false, error: null })
      })
      .catch((err) => {
        if (!cancelled) setState({ data: null, loading: false, error: String(err) })
      })
    return () => {
      cancelled = true
    }
  }, [path])

  return state
}

export const useSummary = () => useJsonData<SummaryData>('data/summary.json')
export const useExecutions = () => useJsonData<RecentExecutionRow[]>('data/executions.json')
export const useFeatures = () => useJsonData<FeatureSummary[]>('data/features.json')
export const useFailures = () => useJsonData<FailureSummary[]>('data/failures.json')
export const useCategories = () => useJsonData<CategorySummary[]>('data/categories.json')
export const useEnvironment = () => useJsonData<EnvironmentInfo>('data/environment.json')
export const useTrends = () => useJsonData<TrendPoint[]>('data/trends.json')
