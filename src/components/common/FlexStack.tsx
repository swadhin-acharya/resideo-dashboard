import { Stack as MuiStack, type StackProps as MuiStackProps } from '@mui/material'
import type { SxProps, Theme } from '@mui/material/styles'
import type { CSSProperties } from 'react'

/**
 * MUI v9's <Stack> no longer accepts `alignItems` / `justifyContent` as
 * first-class props (they must go through `sx`). This wrapper restores the
 * old, much more ergonomic API so the rest of the app can keep using
 * <Stack alignItems="center" justifyContent="space-between"> directly.
 */
type Responsive<T> = T | Partial<Record<'xs' | 'sm' | 'md' | 'lg' | 'xl', T>>

export interface FlexStackProps extends MuiStackProps {
  alignItems?: Responsive<CSSProperties['alignItems']>
  justifyContent?: Responsive<CSSProperties['justifyContent']>
  flexWrap?: Responsive<CSSProperties['flexWrap']>
}

export function Stack({ alignItems, justifyContent, flexWrap, sx, ...rest }: FlexStackProps) {
  const mergedSx: SxProps<Theme> = [
    { alignItems, justifyContent, flexWrap },
    ...(Array.isArray(sx) ? sx : sx ? [sx] : []),
  ] as SxProps<Theme>

  return <MuiStack component="div" sx={mergedSx} {...rest} />
}
