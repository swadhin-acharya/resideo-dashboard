import { Grid, Stack } from '@mui/material'
import { PageHeader } from '../components/layout/PageHeader'
import { ThemeToggleButton, LatestRunSelect, CompareButton, ShareButton } from '../components/layout/HeaderActions'
import { KpiCardsRow } from '../components/overview/KpiCardsRow'
import { ResultsTrendChart } from '../components/overview/ResultsTrendChart'
import { ResultsDistributionChart } from '../components/overview/ResultsDistributionChart'
import { DurationTrendChart } from '../components/overview/DurationTrendChart'
import { RecentExecutionsTable } from '../components/overview/RecentExecutionsTable'
import { TopFailedTests } from '../components/overview/TopFailedTests'
import { CategoriesCard } from '../components/overview/CategoriesCard'
import { EnvironmentCard } from '../components/overview/EnvironmentCard'
import { FeatureHealthGrid } from '../components/overview/FeatureHealthGrid'
import { LoadingState, ErrorState } from '../components/common/LoadingState'
import {
  useSummary,
  useExecutions,
  useFeatures,
  useFailures,
  useCategories,
  useEnvironment,
  useTrends,
} from '../hooks/useDashboardData'

export default function OverviewPage() {
  const summary = useSummary()
  const executions = useExecutions()
  const features = useFeatures()
  const failures = useFailures()
  const categories = useCategories()
  const environment = useEnvironment()
  const trends = useTrends()

  const loading =
    summary.loading || executions.loading || features.loading || failures.loading || categories.loading || environment.loading || trends.loading

  const error = summary.error || executions.error || trends.error

  return (
    <Stack>
      <PageHeader
        title="Overview"
        subtitle="Summary of latest automation execution"
        actions={
          <>
            {summary.data && <LatestRunSelect executionId={summary.data.latestExecutionId} />}
            <CompareButton />
            <ShareButton />
            <ThemeToggleButton />
          </>
        }
      />

      {loading && <LoadingState label="Loading latest automation execution…" />}
      {!loading && error && <ErrorState label="Unable to load dashboard data. Check that public/data/*.json is available." />}

      {!loading && !error && summary.data && (
        <Stack spacing={2.5}>
          <KpiCardsRow summary={summary.data} />

          <Grid container spacing={2.5}>
            <Grid size={{ xs: 12, lg: 4.5 }}>
              <ResultsTrendChart data={trends.data ?? []} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, lg: 4 }}>
              <ResultsDistributionChart current={summary.data.current} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, lg: 3.5 }}>
              <DurationTrendChart data={trends.data ?? []} />
            </Grid>
          </Grid>

          <Grid container spacing={2.5} sx={{ alignItems: 'stretch' }}>
            <Grid size={{ xs: 12, lg: 5 }}>
              <RecentExecutionsTable executions={executions.data ?? []} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, lg: 3.5 }}>
              <TopFailedTests failures={failures.data ?? []} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, lg: 3.5 }}>
              <Stack component="div" spacing={2.5}>
                <CategoriesCard categories={categories.data ?? []} />
                <EnvironmentCard environment={environment.data} />
              </Stack>
            </Grid>
          </Grid>

          <FeatureHealthGrid features={features.data ?? []} />
        </Stack>
      )}
    </Stack>
  )
}
