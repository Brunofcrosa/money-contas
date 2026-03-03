import {
    Component, Input, Output, EventEmitter, computed, signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { Category, CATEGORY_META } from '../../core/models/transaction.model';

@Component({
    selector: 'app-summary-card',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './summary-card.component.html',
    styleUrl: './summary-card.component.css',
    animations: [
        trigger('cardSelect', [
            state('selected', style({ transform: 'translateY(-4px)' })),
            state('normal', style({ transform: 'translateY(0)' })),
            transition('normal <=> selected', animate('200ms ease')),
        ]),
    ],
})
export class SummaryCardComponent {
    @Input({ required: true }) category!: Category;
    @Input() total: number = 0;
    @Input() count: number = 0;
    @Input() isSelected: boolean = false;
    @Output() categoryClicked = new EventEmitter<Category>();

    get meta() {
        return CATEGORY_META[this.category];
    }

    get animState(): string {
        return this.isSelected ? 'selected' : 'normal';
    }

    get formattedTotal(): string {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(this.total);
    }

    onClick(): void {
        this.categoryClicked.emit(this.category);
    }
}
