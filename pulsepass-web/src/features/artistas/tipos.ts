import { z } from 'zod'

export const ArtistSchema = z.object({
  id: z.number(),
  stageName: z.string(),
  genre: z.string(),
  active: z.boolean(),
})

export type Artist = z.infer<typeof ArtistSchema>
