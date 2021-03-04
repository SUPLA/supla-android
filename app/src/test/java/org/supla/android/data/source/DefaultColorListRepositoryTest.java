package org.supla.android.data.source;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.supla.android.data.source.local.ColorListDao;
import org.supla.android.db.ColorListItem;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultColorListRepositoryTest {

    @Mock
    private ColorListDao colorListDao;

    @InjectMocks
    private DefaultColorListRepository defaultColorListRepository;

    @Test
    public void shouldGetColorListItem() {
        // given
        int id = 1;
        boolean group = true;
        int idx = 3;

        ColorListItem expectedResult = mock(ColorListItem.class);
        when(colorListDao.getColorListItem(id, group, idx)).thenReturn(expectedResult);

        // when
        ColorListItem result = defaultColorListRepository.getColorListItem(id, group, idx);

        // then
        assertSame(expectedResult, result);
        verify(colorListDao).getColorListItem(id, group, idx);
        verifyNoMoreInteractions(colorListDao);
    }

    @Test
    public void shouldInsertColorListItemValueWhenNotFound() {
        // given
        int id = 1;
        final boolean group = true;
        int idx = 3;

        ColorListItem colorListItem = mock(ColorListItem.class);
        when(colorListItem.getRemoteId()).thenReturn(id);
        when(colorListItem.getGroup()).thenReturn(group);
        when(colorListItem.getIdx()).thenReturn(idx);

        // when
        defaultColorListRepository.updateColorListItemValue(colorListItem);

        // then
        verify(colorListDao).getColorListItem(id, group, idx);
        verify(colorListDao).insert(colorListItem);
        verifyNoMoreInteractions(colorListDao);

        verify(colorListItem).getRemoteId();
        verify(colorListItem).getGroup();
        verify(colorListItem).getIdx();
        verifyNoMoreInteractions(colorListItem);
    }

    @Test
    public void shouldUpdateColorListItemValueWhenFound() {
        // given
        int id = 1;
        final boolean group = true;
        int idx = 3;

        ColorListItem colorListItem = mock(ColorListItem.class);
        when(colorListDao.getColorListItem(id, group, idx)).thenReturn(colorListItem);

        ColorListItem toUpdate = mock(ColorListItem.class);
        when(toUpdate.getRemoteId()).thenReturn(id);
        when(toUpdate.getGroup()).thenReturn(group);
        when(toUpdate.getIdx()).thenReturn(idx);

        // when
        defaultColorListRepository.updateColorListItemValue(toUpdate);

        // then
        verify(colorListDao).getColorListItem(id, group, idx);
        verify(colorListDao).update(colorListItem);
        verifyNoMoreInteractions(colorListDao);

        verify(toUpdate).getRemoteId();
        verify(toUpdate).getGroup();
        verify(toUpdate).getIdx();
        verify(colorListItem).AssignColorListItem(toUpdate);
        verifyNoMoreInteractions(toUpdate, colorListItem);
    }
}