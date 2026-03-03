import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
    Category,
    CATEGORY_LABELS,
    CATEGORY_COLORS_BG,
    CATEGORY_COLORS_BORDER,
    CATEGORY_COLORS_TEXT,
    CATEGORY_ICONS,
} from '../../../core/models/transaction.model';

@Component({
    selector: 'app-summary-card',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './summary-card.component.html',
})
export class SummaryCardComponent {
    readonly category = input.required<Category>();
    readonly total = input.required<number>();
    readonly count = input<number>(0);
    readonly isActive = input<boolean>(false);

    readonly categorySelected = output<Category>();

    readonly colorMap: Record<Category, string> = {
        ASSINATURAS: 'bg-blue-500',
        ALIMENTACAO: 'bg-green-500',
        VESTIMENTA: 'bg-purple-500',
        LAZER: 'bg-yellow-500',
        FILHOS: 'bg-pink-500',
    };

    readonly labels = CATEGORY_LABELS;
    readonly borderMap = CATEGORY_COLORS_BORDER;
    readonly textMap = CATEGORY_COLORS_TEXT;
    readonly iconMap = CATEGORY_ICONS;

    onSelect(): void {
        this.categorySelected.emit(this.category());
    }

    formatCurrency(value: number): string {
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL',
        }).format(value);
    }
}
