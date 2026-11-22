import { z } from 'zod'

export const VenueSchema = z.object({
  id: z.number(),
  code: z.string(),
  name: z.string(),
  city: z.string(),
  address: z.string(),
  capacity: z.number(),
  active: z.boolean(),
})

export type Venue = z.infer<typeof VenueSchema>
