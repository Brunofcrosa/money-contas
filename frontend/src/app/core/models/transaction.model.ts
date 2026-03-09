export type Category =
  | 'ASSINATURAS'
  | 'ALIMENTACAO'
  | 'FARMACIA'
  | 'CASA'
  | 'VESTIMENTA'
  | 'LAZER'
  | 'GATOS'
  | 'RECEITA';

export type Frequency = 'MONTHLY' | 'ANNUAL';
export type TransactionType = 'INCOME' | 'EXPENSE';
export type PaymentMethod = 'CREDIT' | 'DEBIT' | 'CASH' | 'TRANSFER' | 'PIX' | 'OTHER';

export interface Transaction {
  id: string;
  description: string;
  amount: number;
  category: Category;
  transactionDate: string;
  isRecurrent: boolean;
  frequency?: Frequency;
  installmentsCount?: number;
  installmentNumber?: number;
  type: TransactionType;
  paymentMethod?: PaymentMethod;
  createdAt: string;
}

export interface CreateTransactionRequest {
  description: string;
  amount: number;
  category: Category;
  transactionDate: string;
  isRecurrent: boolean;
  frequency?: Frequency;
  installmentsCount?: number;
  type?: TransactionType;
  paymentMethod?: PaymentMethod;
}

export interface UpdateTransactionRequest {
  description?: string;
  amount?: number;
  category?: Category;
  transactionDate?: string;
  isRecurrent?: boolean;
  frequency?: Frequency;
  installmentsCount?: number;
  type?: TransactionType;
  paymentMethod?: PaymentMethod;
}

export interface TransactionSummary {
  currentBalance: number;
  monthIncome: number;
  monthExpense: number;
  monthCredit: number;
  monthDebit: number;
}

export const CATEGORIES: Category[] = [
  'ASSINATURAS',
  'ALIMENTACAO',
  'FARMACIA',
  'CASA',
  'VESTIMENTA',
  'LAZER',
  'GATOS',
];

export const ALL_CATEGORIES = CATEGORIES;

export interface CategoryMeta {
  label: string;
  color: string;
  icon: string;
  cssVar: string;
}

export const CATEGORY_META: Record<Category, CategoryMeta> = {
  ASSINATURAS: { label: 'Assinaturas', color: '#7C3AED', icon: '📱', cssVar: '--cat-assinaturas' },
  ALIMENTACAO: { label: 'Alimentacao', color: '#059669', icon: '🛒', cssVar: '--cat-alimentacao' },
  FARMACIA: { label: 'Farmacia', color: '#0EA5E9', icon: '💊', cssVar: '--cat-farmacia' },
  CASA: { label: 'Casa', color: '#F97316', icon: '🏠', cssVar: '--cat-casa' },
  VESTIMENTA: { label: 'Vestimenta', color: '#DB2777', icon: '👕', cssVar: '--cat-vestimenta' },
  LAZER: { label: 'Lazer', color: '#D97706', icon: '🎮', cssVar: '--cat-lazer' },
  GATOS: { label: 'Gatos', color: '#0284C7', icon: '🐱', cssVar: '--cat-gato' },
  RECEITA: { label: 'Receita', color: '#10B981', icon: '💰', cssVar: '--cat-receita' },
};

export const CATEGORY_LABELS: Record<Category, string> = {
  ASSINATURAS: 'Assinaturas',
  ALIMENTACAO: 'Alimentacao',
  FARMACIA: 'Farmacia',
  CASA: 'Casa',
  VESTIMENTA: 'Vestimenta',
  LAZER: 'Lazer',
  GATOS: 'Gatos',
  RECEITA: 'Receita',
};

export const CATEGORY_COLORS_HEX: Record<Category, string> = {
  ASSINATURAS: '#7C3AED',
  ALIMENTACAO: '#059669',
  FARMACIA: '#0EA5E9',
  CASA: '#F97316',
  VESTIMENTA: '#DB2777',
  LAZER: '#D97706',
  GATOS: '#0284C7',
  RECEITA: '#10B981',
};

export const FREQUENCY_LABELS: Record<Frequency, string> = {
  MONTHLY: 'Mensal',
  ANNUAL: 'Anual',
};
