package org.supla.android.data.source;

import android.database.Cursor;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.supla.android.data.source.local.UserIconDao;
import org.supla.android.db.SuplaContract;
import org.supla.android.images.ImageCacheProvider;
import org.supla.android.images.ImageId;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUserIconRepositoryTest {

    @Mock
    private UserIconDao userIconDao;
    @Mock
    private ImageCacheProvider imageCacheProvider;

    private DefaultUserIconRepository userIconRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userIconRepository = new DefaultUserIconRepository(userIconDao, imageCacheProvider, 0);
    }
    
    @Test
    public void shouldNotAddUserIconsWhenWrongId() {
        // given
        int id = -2;
        byte[] img1 = new byte[0];
        byte[] img2 = new byte[0];
        byte[] img3 = new byte[0];
        byte[] img4 = new byte[0];

        // when
        boolean result = userIconRepository.addUserIcons(id, img1, img2, img3, img4);

        // then
        assertFalse(result);
        verifyZeroInteractions(userIconDao, imageCacheProvider);
    }

    @Test
    public void shouldNotAddUserIconsWhenAllImageAreNull() {
        // given
        int id = 5;
        byte[] img1 = null;
        byte[] img2 = null;
        byte[] img3 = null;
        byte[] img4 = null;

        // when
        boolean result = userIconRepository.addUserIcons(id, img1, img2, img3, img4);

        // then
        assertFalse(result);
        verifyZeroInteractions(userIconDao, imageCacheProvider);
    }

    @Test
    public void shouldAddUserIcons() {
        // given
        int id = 5;
        byte[] img1 = new byte[0];
        byte[] img2 = new byte[0];
        byte[] img3 = new byte[0];
        byte[] img4 = new byte[0];

        // when
        boolean result = userIconRepository.addUserIcons(id, img1, img2, img3, img4);

        // then
        assertTrue(result);

        ArgumentCaptor<UserIconDao.Image[]> insertedImagesCaptor = ArgumentCaptor.forClass(UserIconDao.Image[].class);
        verify(userIconDao).insert(eq(id), insertedImagesCaptor.capture());
        ArgumentCaptor<UserIconDao.Image> cachedImagesCaptor = ArgumentCaptor.forClass(UserIconDao.Image.class);
        verify(imageCacheProvider, times(4)).addImage(eq(id), cachedImagesCaptor.capture());
        verifyNoMoreInteractions(userIconDao, imageCacheProvider);

        UserIconDao.Image[] insertedImages = insertedImagesCaptor.getValue();
        assertEquals(4, insertedImages.length);
        assertImage(insertedImages[0], SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1, img1, 1);
        assertImage(insertedImages[1], SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2, img2, 2);
        assertImage(insertedImages[2], SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3, img3, 3);
        assertImage(insertedImages[3], SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4, img4, 4);

        List<UserIconDao.Image> cachedImages = cachedImagesCaptor.getAllValues();
        assertEquals(4, cachedImages.size());
        assertImage(cachedImages.get(0), SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1, img1, 1);
        assertImage(cachedImages.get(1), SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2, img2, 2);
        assertImage(cachedImages.get(2), SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3, img3, 3);
        assertImage(cachedImages.get(3), SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4, img4, 4);
    }

    @Test
    public void shouldDeleteUserIcons() {
        // when
        userIconRepository.deleteUserIcons();
        // then
        verify(userIconDao).delete();
        verifyNoMoreInteractions(userIconDao);
        verifyZeroInteractions(imageCacheProvider);
    }

    @Test
    public void shouldLoadUserIconsIntoCache() {
        // given
        int id = 3;
        int idColumnIndex = 2;
        byte[] image = new byte[1];
        int imageColumnIndex = 1;

        Cursor cursor = mockCursor(id, idColumnIndex, image, imageColumnIndex);
        when(userIconDao.getUserIcons()).thenReturn(cursor);

        // when
        userIconRepository.loadUserIconsIntoCache();

        // then
        ArgumentCaptor<ImageId> imageIdArgumentCaptor = ArgumentCaptor.forClass(ImageId.class);
        ArgumentCaptor<byte[]> imageArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(imageCacheProvider).addImage(imageIdArgumentCaptor.capture(), imageArgumentCaptor.capture());

        ImageId imageId = imageIdArgumentCaptor.getValue();
        byte[] imageBytes = imageArgumentCaptor.getValue();
        assertEquals(id, imageId.getId());
        assertEquals(3, imageId.getId());
        assertSame(imageBytes, image);

        verifyCursor(cursor, imageColumnIndex, idColumnIndex);
    }

    @Test
    public void shouldNotLoadUserIconsIntoCacheWhenImageLengthIsZero() {
        // given
        int id = 3;
        int idColumnIndex = 2;
        byte[] image = new byte[0];
        int imageColumnIndex = 1;

        Cursor cursor = mockCursor(id, idColumnIndex, image, imageColumnIndex);
        when(userIconDao.getUserIcons()).thenReturn(cursor);

        // when
        userIconRepository.loadUserIconsIntoCache();

        // then
        verify(imageCacheProvider, never()).addImage(any(), any());
        verifyCursor(cursor, imageColumnIndex, idColumnIndex);
    }

    private void assertImage(UserIconDao.Image image, String column, byte[] value, int subId) {
        assertEquals(column, image.column);
        assertSame(value, image.value);
        assertEquals(subId, image.subId);
    }

    private Cursor mockCursor(int id, int idColumnIndex, byte[] image, int imageColumnIndex) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getColumnIndex(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1)).thenReturn(imageColumnIndex);
        when(cursor.getBlob(imageColumnIndex)).thenReturn(image);
        when(cursor.getColumnIndex(SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID)).thenReturn(idColumnIndex);
        when(cursor.getInt(idColumnIndex)).thenReturn(id);

        return cursor;
    }

    private void verifyCursor(Cursor cursor, int imageColumnIndex, int idColumnIndex) {
        verify(cursor).moveToFirst();
        verify(cursor).getColumnIndex(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1);
        verify(cursor).getColumnIndex(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2);
        verify(cursor).getColumnIndex(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3);
        verify(cursor).getColumnIndex(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4);
        verify(cursor, times(4)).getColumnIndex(SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID);
        verify(cursor).getBlob(imageColumnIndex);
        verify(cursor, times(3)).getBlob(0);
        verify(cursor, times(4)).getInt(idColumnIndex);
        verify(cursor).moveToNext();
        verify(cursor).close();
        verifyNoMoreInteractions(cursor);
    }
}
