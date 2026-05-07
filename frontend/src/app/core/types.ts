export type AuthResponse = {
  token: string;
  email: string;
  role: string;
};

export type PropertyCreateRequest = {
  title: string;
  description: string;
  city: string;
  country: string;
  valuationUsd: number;
};

export type PropertyResponse = PropertyCreateRequest & {
  id: string;
  createdAt: string;
};

export type OfferingCreateRequest = {
  propertyId: string;
  totalTokens: number;
  tokenPriceUsd: number;
};

export type OfferingResponse = OfferingCreateRequest & {
  id: string;
  tokensSold: number;
  status: string;
  createdAt: string;
};

export type InvestmentRequest = {
  offeringId: string;
  tokensRequested: number;
};

export type InvestmentResponse = InvestmentRequest & {
  id: string;
  investorEmail: string;
  amountUsd: number;
  status: string;
  createdAt: string;
};

