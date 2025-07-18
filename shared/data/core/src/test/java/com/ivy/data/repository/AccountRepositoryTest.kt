package com.ivy.data.repository

import com.ivy.base.TestDispatchersProvider
import com.ivy.data.DataObserver
import com.ivy.data.db.dao.fake.FakeSettingsDao
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.write.WriteAccountDao
import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.repository.fake.fakeRepositoryMemoFactory
import com.ivy.data.repository.mapper.AccountMapper
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class AccountRepositoryTest {
    val accountDao = mockk<AccountDao>()
    val writeAccountDao = mockk<WriteAccountDao>()
    val writeEventBus = mockk<DataObserver>(relaxed = true)

    private lateinit var repository: AccountRepository

    @Before
    fun setup() {
        val settingsDao = FakeSettingsDao()
        repository = AccountRepository(
            mapper = AccountMapper(
                CurrencyRepository(
                    settingsDao = settingsDao,
                    writeSettingsDao = settingsDao,
                    dispatchersProvider = TestDispatchersProvider
                )
            ),
            accountDao = accountDao,
            writeAccountDao = writeAccountDao,
            dispatchersProvider = TestDispatchersProvider,
            memoFactory = fakeRepositoryMemoFactory(),
        )
    }

    @Test
    fun `find by id - null AccountEntity`() = runTest {
        // given
        val accountId = AccountId(UUID.randomUUID())
        coEvery { accountDao.findById(accountId.value) } returns null

        // when
        val res = repository.findById(accountId)

        // then
        res shouldBe null
    }

    @Test
    fun `find by id - valid AccountEntity`() = runTest {
        // given
        val accountId = AccountId(UUID.randomUUID())
        coEvery { accountDao.findById(accountId.value) } returns AccountEntity(
            name = "Bank",
            currency = "BGN",
            color = 1,
            icon = null,
            orderNum = 1.0,
            includeInBalance = true,
            isSynced = true,
            isDeleted = false,
            id = accountId.value
        )

        // when
        val res = repository.findById(accountId)

        // then
        res shouldBe Account(
            id = accountId,
            name = NotBlankTrimmedString.unsafe("Bank"),
            asset = AssetCode.unsafe("BGN"),
            color = ColorInt(1),
            icon = null,
            includeInBalance = true,
            orderNum = 1.0,
            isVisible = true
        )
    }

    @Test
    fun `find by id - invalid AccountEntity`() = runTest {
        // given
        val accountId = AccountId(UUID.randomUUID())
        coEvery { accountDao.findById(accountId.value) } returns AccountEntity(
            name = " ",
            currency = "BGN",
            color = 1,
            icon = null,
            orderNum = 2.0,
            includeInBalance = true,
            isSynced = true,
            isDeleted = false,
            id = accountId.value
        )

        // when
        val res = repository.findById(accountId)

        // then
        res shouldBe null
    }

    @Test
    fun `find all not deleted - empty list`() = runTest {
        // given
        coEvery { accountDao.findAll(false) } returns emptyList()

        // when
        val res = repository.findAll()

        // then
        res shouldBe emptyList()
    }

    @Test
    fun `find all not deleted - list of valid accounts`() = runTest {
        // given
        val account1Id = AccountId(UUID.randomUUID())
        val account2Id = AccountId(UUID.randomUUID())
        coEvery { accountDao.findAll(false) } returns listOf(
            AccountEntity(
                name = "Bank",
                currency = "BGN",
                color = 1,
                icon = null,
                orderNum = 1.0,
                includeInBalance = true,
                isSynced = true,
                isDeleted = false,
                id = account1Id.value
            ),
            AccountEntity(
                name = "Cash",
                currency = "BGN",
                color = 2,
                icon = null,
                orderNum = 2.0,
                includeInBalance = true,
                isSynced = true,
                isDeleted = false,
                id = account2Id.value
            )
        )

        // when
        val res = repository.findAll()

        // then
        res shouldBe listOf(
            Account(
                id = account1Id,
                name = NotBlankTrimmedString.unsafe("Bank"),
                asset = AssetCode.unsafe("BGN"),
                color = ColorInt(1),
                icon = null,
                includeInBalance = true,
                orderNum = 1.0,
                isVisible = true
            ),
            Account(
                id = account2Id,
                name = NotBlankTrimmedString.unsafe("Cash"),
                asset = AssetCode.unsafe("BGN"),
                color = ColorInt(2),
                icon = null,
                includeInBalance = true,
                orderNum = 2.0,
                isVisible = true
            )
        )
    }

    @Test
    fun `find all not deleted - list with valid and invalid accounts`() = runTest {
        // given
        val account1Id = AccountId(UUID.randomUUID())
        val account2Id = AccountId(UUID.randomUUID())
        coEvery { accountDao.findAll(false) } returns listOf(
            AccountEntity(
                name = "Bank",
                currency = "BGN",
                color = 1,
                icon = null,
                orderNum = 1.0,
                includeInBalance = true,
                isSynced = true,
                isDeleted = false,
                id = account1Id.value
            ),
            AccountEntity(
                name = "  ",
                currency = "BGN",
                color = 2,
                icon = null,
                orderNum = 2.0,
                includeInBalance = true,
                isSynced = true,
                isDeleted = false,
                id = account2Id.value
            )
        )

        // when
        val res = repository.findAll()

        // then
        res shouldBe listOf(
            Account(
                id = account1Id,
                name = NotBlankTrimmedString.unsafe("Bank"),
                asset = AssetCode.unsafe("BGN"),
                color = ColorInt(1),
                icon = null,
                includeInBalance = true,
                orderNum = 1.0,
                isVisible = true
            )
        )
    }

    @Test
    fun `finds max order num - no accounts`() = runTest {
        // given
        coEvery { accountDao.findMaxOrderNum() } returns null

        // when
        val orderNum = repository.findMaxOrderNum()

        // then
        orderNum shouldBe 0.0
    }

    @Test
    fun `finds max order num - existing account`() = runTest {
        // given
        coEvery { accountDao.findMaxOrderNum() } returns 42.0

        // when
        val orderNum = repository.findMaxOrderNum()

        // then
        orderNum shouldBe 42.0
    }

    @Test
    fun save() = runTest {
        // given
        val accountId = AccountId(UUID.randomUUID())
        coEvery { writeAccountDao.save(any()) } just runs
        val account = Account(
            id = accountId,
            name = NotBlankTrimmedString.unsafe("Bank"),
            asset = AssetCode.unsafe("BGN"),
            color = ColorInt(1),
            icon = null,
            includeInBalance = true,
            orderNum = 1.0,
            isVisible = true
        )

        // when
        repository.save(account)

        // then
        coVerify(exactly = 1) {
            writeAccountDao.save(
                AccountEntity(
                    name = "Bank",
                    currency = "BGN",
                    color = 1,
                    icon = null,
                    orderNum = 1.0,
                    includeInBalance = true,
                    isSynced = true,
                    isDeleted = false,
                    id = accountId.value
                )
            )
        }
    }

    @Test
    fun `save many`() = runTest {
        // given
        val account1Id = AccountId(UUID.randomUUID())
        val account2Id = AccountId(UUID.randomUUID())
        coEvery { writeAccountDao.saveMany(any()) } just runs
        val accounts = listOf(
            Account(
                id = account1Id,
                name = NotBlankTrimmedString.unsafe("Bank"),
                asset = AssetCode.unsafe("BGN"),
                color = ColorInt(1),
                icon = null,
                includeInBalance = true,
                orderNum = 1.0,
                isVisible = true
            ),
            Account(
                id = account2Id,
                name = NotBlankTrimmedString.unsafe("Cash"),
                asset = AssetCode.unsafe("BGN"),
                color = ColorInt(2),
                icon = null,
                includeInBalance = true,
                orderNum = 2.0,
                isVisible = true
            )
        )

        // when
        repository.saveMany(accounts)

        // then
        coVerify(exactly = 1) {
            writeAccountDao.saveMany(
                listOf(
                    AccountEntity(
                        name = "Bank",
                        currency = "BGN",
                        color = 1,
                        icon = null,
                        orderNum = 1.0,
                        includeInBalance = true,
                        isSynced = true,
                        isDeleted = false,
                        id = account1Id.value
                    ),
                    AccountEntity(
                        name = "Cash",
                        currency = "BGN",
                        color = 2,
                        icon = null,
                        orderNum = 2.0,
                        includeInBalance = true,
                        isSynced = true,
                        isDeleted = false,
                        id = account2Id.value
                    )
                )
            )
        }
    }

    @Test
    fun `delete by id`() = runTest {
        // given
        val accountId = AccountId(UUID.randomUUID())
        coEvery { writeAccountDao.deleteById(any()) } just runs

        // when
        repository.deleteById(accountId)

        // then
        coVerify(exactly = 1) {
            writeAccountDao.deleteById(accountId.value)
        }
    }

    @Test
    fun `delete all`() = runTest {
        // given
        coEvery { writeAccountDao.deleteAll() } just runs

        // when
        repository.deleteAll()

        // then
        coVerify(exactly = 1) {
            writeAccountDao.deleteAll()
        }
    }
}
